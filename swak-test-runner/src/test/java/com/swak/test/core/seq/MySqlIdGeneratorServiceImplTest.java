package com.swak.test.core.seq;

import com.swak.core.seq.MySqlIdGeneratorServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MySqlIdGeneratorServiceImpl} 的号段拉取与切换逻辑。
 * <p>
 * 生产实现直接依赖 {@link JdbcTemplate}，这里用一个继承自 JdbcTemplate 的
 * 「内存表」替身（只有 query / update 两个方法被重写），
 * update 用 CAS 模拟乐观锁，不连接任何真实数据库。
 * <p>
 * 注意：本类会创建 {@link java.util.concurrent.ThreadPoolExecutor}（仅异步模式真正起线程），
 * 每个用例结束后通过反射 shutdown，避免污染构建进程。
 */
@DisplayName("MySQL 号段 ID 生成器")
class MySqlIdGeneratorServiceImplTest {

    private MySqlIdGeneratorServiceImpl service;

    @AfterEach
    void tearDown() throws Exception {
        if (service == null) {
            return;
        }
        Field field = MySqlIdGeneratorServiceImpl.class.getDeclaredField("taskExecutor");
        field.setAccessible(true);
        Object executor = field.get(service);
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdownNow();
        }
    }

    @Test
    @DisplayName("afterPropertiesSet：缺少 bizTag / 数据源 / 表名时都抛 RuntimeException")
    void afterPropertiesSetValidatesRequiredFields() {
        MySqlIdGeneratorServiceImpl noBizTag = new MySqlIdGeneratorServiceImpl();
        noBizTag.setSeqTableName("id_segment");
        noBizTag.setJdbcTemplate(mock(JdbcTemplate.class));
        assertThatThrownBy(noBizTag::afterPropertiesSet)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("业务类型");

        MySqlIdGeneratorServiceImpl noJdbc = new MySqlIdGeneratorServiceImpl();
        noJdbc.setBizTag("order");
        noJdbc.setSeqTableName("id_segment");
        assertThatThrownBy(noJdbc::afterPropertiesSet)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("数据源");

        MySqlIdGeneratorServiceImpl noTable = new MySqlIdGeneratorServiceImpl();
        noTable.setBizTag("order");
        noTable.setJdbcTemplate(mock(JdbcTemplate.class));
        assertThatThrownBy(noTable::afterPropertiesSet)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("seqTableName");
    }

    @Test
    @DisplayName("startInit=false：init 不拉取号段，nextId 直接 NPE（当前实现无兜底）")
    void initSkippedWhenStartInitDisabled() throws Exception {
        service = newService(new FakeIdTable(1000L, 100L), false);
        service.setStartInit(false);
        service.afterPropertiesSet();
        service.init();

        assertThatThrownBy(service::nextId).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("同步模式：初始 ID 为号段下界 + 1，跨段切换后仍唯一递增")
    void syncModeGeneratesMonotonicIds() throws Exception {
        service = newService(new FakeIdTable(1000L, 100L), false);
        service.afterPropertiesSet();
        service.init();

        List<Long> ids = drain(260);

        // init 时 currentId = minId(1000)，因此第一个 ID 是 1001
        assertThat(ids.get(0)).isEqualTo(1001L);
        assertUniqueAndIncreasing(ids);
    }

    @Test
    @DisplayName("异步模式：预加载下一段，跨段切换后仍唯一递增")
    void asyncModeGeneratesMonotonicIds() throws Exception {
        service = newService(new FakeIdTable(1000L, 100L), true);
        service.afterPropertiesSet();
        service.init();

        List<Long> ids = drain(260);

        assertThat(ids.get(0)).isEqualTo(1001L);
        assertUniqueAndIncreasing(ids);
    }

    @Test
    @DisplayName("更新号段失败一次后重试成功：拿到的是重试那次的号段")
    void retryAfterSingleUpdateFailureSucceeds() throws Exception {
        FakeIdTable table = new FakeIdTable(1000L, 100L, call -> call > 1);

        service = newService(table, false);
        service.afterPropertiesSet();
        service.init();

        assertThat(table.updateCalls.get()).isEqualTo(2);
        assertThat(service.nextId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("重试计数不会跨调用累积：每次拉取都独立享有 3 次重试机会")
    void retryCountIsResetPerLoad() throws Exception {
        // 奇数次 update 失败、偶数次成功：init 与后续每次拉段都要「失败一次再成功」
        FakeIdTable table = new FakeIdTable(1000L, 100L, call -> call % 2 == 0);

        service = newService(table, false);
        service.afterPropertiesSet();
        service.init();

        List<Long> ids = drain(260);

        assertUniqueAndIncreasing(ids);
        // 260 个 ID 至少要跨 3 个号段，每次都要重试一次
        assertThat(table.updateCalls.get()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("更新连续失败超过 3 次：放弃重试，init 抛出带 bizTag 与表名的 IllegalStateException")
    void giveUpAfterThreeRetries() throws Exception {
        FakeIdTable table = new FakeIdTable(1000L, 100L, call -> false);

        service = newService(table, false);

        assertThatThrownBy(() -> {
            service.afterPropertiesSet();
            service.init();
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("号段拉取失败")
                .hasMessageContaining("bizTag=order")
                .hasMessageContaining("table=id_segment");
        assertThat(table.updateCalls.get()).isEqualTo(4);
    }

    private MySqlIdGeneratorServiceImpl newService(JdbcTemplate jdbc, boolean async) {
        MySqlIdGeneratorServiceImpl svc = new MySqlIdGeneratorServiceImpl();
        svc.setBizTag("order");
        svc.setSeqTableName("id_segment");
        svc.setJdbcTemplate(jdbc);
        svc.setAsynLoadingSegment(async);
        return svc;
    }

    private List<Long> drain(int count) {
        List<Long> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(service.nextId());
        }
        return ids;
    }

    private static void assertUniqueAndIncreasing(List<Long> ids) {
        assertThat(new HashSet<>(ids)).hasSize(ids.size());
        for (int i = 1; i < ids.size(); i++) {
            assertThat(ids.get(i)).isGreaterThan(ids.get(i - 1));
        }
    }

    /**
     * 只重写生产代码真正调用的两个重载，模拟一张带乐观锁的 id_segment 表。
     * <p>
     * {@code succeedOnCall} 用来控制第 N 次 update 是否返回 1（用来覆盖失败重试分支）。
     */
    private static final class FakeIdTable extends JdbcTemplate {

        private final AtomicLong storedMaxId;
        private final long step;
        private final IntPredicate succeedOnCall;
        private final AtomicInteger updateCalls = new AtomicInteger();
        private final AtomicInteger queryCalls = new AtomicInteger();

        private FakeIdTable(long initialMaxId, long step) {
            this(initialMaxId, step, call -> true);
        }

        private FakeIdTable(long initialMaxId, long step, IntPredicate succeedOnCall) {
            this.storedMaxId = new AtomicLong(initialMaxId);
            this.step = step;
            this.succeedOnCall = succeedOnCall;
        }

        @Override
        public void query(String sql, RowCallbackHandler rch, Object... args) {
            queryCalls.incrementAndGet();
            try {
                rch.processRow(resultSet(storedMaxId.get(), step));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int update(String sql, Object... args) {
            int call = updateCalls.incrementAndGet();
            long newMaxId = ((Number) args[0]).longValue();
            long oldMaxId = ((Number) args[3]).longValue();
            if (!succeedOnCall.test(call)) {
                return 0;
            }
            return storedMaxId.compareAndSet(oldMaxId, newMaxId) ? 1 : 0;
        }

        private static ResultSet resultSet(long maxId, long step) throws SQLException {
            ResultSet rs = mock(ResultSet.class);
            when(rs.getLong("p_step")).thenReturn(step);
            when(rs.getLong("max_id")).thenReturn(maxId);
            return rs;
        }
    }
}
