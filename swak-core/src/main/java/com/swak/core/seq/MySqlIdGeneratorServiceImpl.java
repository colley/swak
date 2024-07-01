package com.swak.core.seq;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CREATE TABLE `id_segment` (
 * `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
 * `biz_tag` varchar(50) DEFAULT NULL COMMENT '业务标识',
 * `max_id` bigint(20) DEFAULT NULL COMMENT '分配的id号段的最大值',
 * `p_step` bigint(20) DEFAULT NULL COMMENT '步长',
 * `last_update_time` datetime DEFAULT NULL,
 * `current_update_time` datetime DEFAULT NULL,
 * PRIMARY KEY (`id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='号段存储表';
 * 
 * @author mayuanchao
 */

@Slf4j
public class MySqlIdGeneratorServiceImpl implements IdGenerator, InitializingBean {

    // 创建线程池
    private ExecutorService taskExecutor;

    private final static String SEQ_QUERY_SQL =
        "select p_step ,max_id ,last_update_time,current_update_time from %s where biz_tag=?";
    private final static String SEQ_UPDATE_SQL =
        "update %s set max_id=?,last_update_time=?,current_update_time=now() where biz_tag=? and max_id=?";

    public MySqlIdGeneratorServiceImpl() {

    }

    private volatile IdSegment[] segment = new IdSegment[2]; // 这两段用来存储每次拉升之后的最大值
    private volatile boolean sw;
    private AtomicLong currentId;

    private ReentrantLock lock = new ReentrantLock();
    private volatile FutureTask<Boolean> asynLoadSegmentTask = null;
    /**
     * 数据源
     */
    private JdbcTemplate jdbcTemplate;
    /**
     * 业务代码
     */
    private String bizTag;
    /**
     * 是否异步 加载新的段位
     */
    private boolean asynLoadingSegment;

    private boolean startInit = true;

    // seq的表名
    private String seqTableName;

    /**
     * 跟踪调用方法调用次数
     */
    private ThreadLocal<AtomicInteger> invokeNum = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger(0);
        };
    };

    public void init() {
        if (!startInit) {
            return;
        }
        segment[0] = doUpdateNextSegment(bizTag);
        setSw(false);
        currentId = new AtomicLong(segment[index()].getMinId()); // 初始id
        log.error("id 生成器启动成功");
    }

    @Override
    public Long nextId() {
        if (asynLoadingSegment) {
            return getIdByAsync();
        } else {
            return getIdBySync();
        }
    }

    /**
     * 异步获取ID
     * 
     * @return
     */
    private Long getIdByAsync() {

        if (segment[index()].getMiddleId() <= currentId.longValue() && isNotLoadOfNextsegment()
            && asynLoadSegmentTask == null) {
            lock.lock();
            try {
                if (segment[index()].getMiddleId() <= currentId.longValue()) {
                    // 前一段使用了50%
                    asynLoadSegmentTask = new FutureTask<>(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            final int currentIndex = reIndex();
                            segment[currentIndex] = doUpdateNextSegment(bizTag);
                            return true;
                        }

                    });
                    taskExecutor.submit(asynLoadSegmentTask);
                    log.error("启动异步加载任务成功");
                }

            } finally {
                lock.unlock();
            }
        }

        if (segment[index()].getMaxId() <= currentId.longValue()) {
            lock.lock();
            try {
                if (segment[index()].getMaxId() <= currentId.longValue()) {
                    boolean loadingResult = false;
                    try {
                        loadingResult = asynLoadSegmentTask.get(500, TimeUnit.MILLISECONDS);
                        if (loadingResult) {
                            setSw(!isSw()); // 切换
                            currentId = new AtomicLong(segment[index()].getMinId()); // 进行切换
                            asynLoadSegmentTask = null;
                        }
                    } catch (Exception e) {
                        log.error("切换新老段位失败", e);
                        loadingResult = false;
                        asynLoadSegmentTask = null;
                    }
                    if (!loadingResult) {
                        while (isNotLoadOfNextsegment()) {
                            // 强制同步切换
                            final int currentIndex = reIndex();
                            segment[currentIndex] = doUpdateNextSegment(bizTag);
                        }
                        setSw(!isSw()); // 切换
                        currentId = new AtomicLong(segment[index()].getMinId()); // 进行切换

                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return currentId.incrementAndGet();

    }

    private boolean isNotLoadOfNextsegment() {
        if (segment[reIndex()] == null) {
            return true;
        }
        if (segment[reIndex()].getMinId() < segment[index()].getMinId()) {
            return true;
        }
        return false;
    }

    /**
     * 同步获取id
     * 
     * @return
     */
    private long getIdBySync() {
        if (segment[index()].getMiddleId() <= currentId.longValue() && isNotLoadOfNextsegment()) { // 需要加载了
            lock.lock();
            try {
                if (segment[index()].getMiddleId() <= currentId.longValue()
                    && isNotLoadOfNextsegment()) {
                    // 使用50%以上，并且没有加载成功过，就进行加载
                    final int currentIndex = reIndex();
                    segment[currentIndex] = doUpdateNextSegment(bizTag);
                }
            } finally {
                lock.unlock();
            }
        }

        if (segment[index()].getMaxId() <= currentId.longValue()) { // 需要进行切换了
            lock.lock();
            try {
                if (segment[index()].getMaxId() <= currentId.longValue()) {
                    while (isNotLoadOfNextsegment()) {
                        // 使用50%以上，并且没有加载成功过，就进行加载,直到在功
                        final int currentIndex = reIndex();
                        segment[currentIndex] = doUpdateNextSegment(bizTag);
                    }
                    setSw(!isSw()); // 切换
                    currentId = new AtomicLong(segment[index()].getMinId()); // 进行切换

                }

            } finally {
                lock.unlock();
            }
        }
        return currentId.incrementAndGet();

    }

    private boolean isSw() {
        return sw;
    }

    private void setSw(boolean sw) {
        this.sw = sw;
    }

    private int index() {
        if (isSw()) {
            return 1;
        } else {
            return 0;
        }
    }

    private int reIndex() {
        if (isSw()) {
            return 0;
        } else {
            return 1;
        }
    }

    private IdSegment doUpdateNextSegment(String bizTag) {
        try {
            return updateId(bizTag);
        } catch (Exception e) {
            log.error("更新下一个段位失败", e);
        }
        return null;
    }

    private IdSegment updateId(String bizTag) throws Exception {
        String querySql = String.format(SEQ_QUERY_SQL, this.seqTableName);
        String updateSql = String.format(SEQ_UPDATE_SQL, this.seqTableName);
        final IdSegment currentSegment = new IdSegment();
        this.jdbcTemplate.query(querySql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {

                Long step = null;
                Long currentMaxId = null;
                step = rs.getLong("p_step");
                currentMaxId = rs.getLong("max_id");

                Date lastUpdateTime = new Date();
                if (rs.getTimestamp("last_update_time") != null) {
                    lastUpdateTime = (java.util.Date) rs.getTimestamp("last_update_time");
                }

                Date currentUpdateTime = new Date();
                if (rs.getTimestamp("current_update_time") != null) {
                    currentUpdateTime = (java.util.Date) rs.getTimestamp("current_update_time");
                }
                currentSegment.setStep(step);
                currentSegment.setMaxId(currentMaxId);
                currentSegment.setLastUpdateTime(lastUpdateTime);
                currentSegment.setCurrentUpdateTime(currentUpdateTime);
            }
        }, bizTag);
        Long newMaxId = currentSegment.getMaxId() + currentSegment.getStep();
        int row = this.jdbcTemplate.update(updateSql, new Object[] {newMaxId,
            currentSegment.getCurrentUpdateTime(), bizTag, currentSegment.getMaxId()});
        if (row == 1) {
            IdSegment newSegment = new IdSegment();
            newSegment.setStep(currentSegment.getStep());
            newSegment.setMaxId(newMaxId);
            return newSegment;

        } else {
            AtomicInteger num = invokeNum.get();
            int newValue = num.incrementAndGet();
            invokeNum.set(num);
            // 递归更新，如果三次还没有 更新成功,直接打印错误日志，并返回，将原有调用次数归位
            if (newValue > 3) {
                log.error("更新下一个段位返回row=" + row + "失败", +newMaxId);
                num.set(0);
                invokeNum.set(num);
            } else {
                updateId(bizTag);
            }
            return null;
        }

    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }

    public void setAsynLoadingSegment(boolean asynLoadingSegment) {
        this.asynLoadingSegment = asynLoadingSegment;
    }



    public void setSeqTableName(String seqTableName) {
        this.seqTableName = seqTableName;
    }

    public void setStartInit(boolean startInit) {
        this.startInit = startInit;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isEmpty(this.bizTag)) {
            throw new RuntimeException("业务类型不能为空");
        }
        if (this.jdbcTemplate == null) {
            throw new RuntimeException("数据源不能为空");
        }
        if (StringUtils.isEmpty(this.seqTableName)) {
            throw new RuntimeException("seqTableName不能为空");
        }
        this.taskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    }

}
