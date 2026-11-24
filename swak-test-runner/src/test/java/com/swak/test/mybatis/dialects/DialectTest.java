package com.swak.test.mybatis.dialects;

import com.swak.jdbc.dialects.DB2Dialect;
import com.swak.jdbc.dialects.Dialect;
import com.swak.jdbc.dialects.DialectDbType;
import com.swak.jdbc.dialects.DialectModel;
import com.swak.jdbc.dialects.DialectRegistry;
import com.swak.jdbc.dialects.MySqlDialect;
import com.swak.jdbc.dialects.OracleDialect;
import com.swak.jdbc.dialects.PostgreDialect;
import com.swak.jdbc.dialects.SQLServerDialect;
import com.swak.jdbc.parser.ParameterMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 分页方言（{@link Dialect}）与注册表（{@link DialectRegistry}）回归。
 */
@DisplayName("分页方言")
class DialectTest {

    @Test
    @DisplayName("从 JDBC URL 识别数据库类型")
    void detectsTypeFromUrl() {
        assertThat(DialectRegistry.getDialectType("jdbc:mysql://h:3306/db")).isEqualTo(DialectDbType.MYSQL);
        assertThat(DialectRegistry.getDialectType("jdbc:cobar://h/db")).isEqualTo(DialectDbType.MYSQL);
        assertThat(DialectRegistry.getDialectType("jdbc:mariadb://h/db")).isEqualTo(DialectDbType.MARIADB);
        assertThat(DialectRegistry.getDialectType("jdbc:oracle:thin:@h:1521:orcl")).isEqualTo(DialectDbType.ORACLE);
        assertThat(DialectRegistry.getDialectType("jdbc:sqlserver2012://h:1433;db")).isEqualTo(DialectDbType.SQL_SERVER);
        assertThat(DialectRegistry.getDialectType("jdbc:postgresql://h/db")).isEqualTo(DialectDbType.POSTGRE_SQL);
        assertThat(DialectRegistry.getDialectType("jdbc:hsqldb:mem:x")).isEqualTo(DialectDbType.HSQL);
        assertThat(DialectRegistry.getDialectType("jdbc:db2://h/db")).isEqualTo(DialectDbType.DB2);
        assertThat(DialectRegistry.getDialectType("jdbc:sqlite:/tmp/x.db")).isEqualTo(DialectDbType.SQLITE);
        assertThat(DialectRegistry.getDialectType("jdbc:h2:mem:x")).isEqualTo(DialectDbType.H2);
        assertThat(DialectRegistry.getDialectType("jdbc:dm8://h:5236")).isEqualTo(DialectDbType.DM);
        assertThat(DialectRegistry.getDialectType("jdbc:xugu://h/db")).isEqualTo(DialectDbType.XU_GU);
        assertThat(DialectRegistry.getDialectType("jdbc:kingbase8://h/db")).isEqualTo(DialectDbType.KINGBASE_ES);
        assertThat(DialectRegistry.getDialectType("jdbc:phoenix:h:2181")).isEqualTo(DialectDbType.PHOENIX);
        // GAUSS / GBASE / OSCAR / OCEAN_BASE 分支区分大小写，URL 必须小写才能命中。
        assertThat(DialectRegistry.getDialectType("jdbc:zenith:@h")).isEqualTo(DialectDbType.GAUSS);
        assertThat(DialectRegistry.getDialectType("jdbc:gbase://h/db")).isEqualTo(DialectDbType.GBASE);
        assertThat(DialectRegistry.getDialectType("jdbc:clickhouse://h:8123/db")).isEqualTo(DialectDbType.CLICK_HOUSE);
        assertThat(DialectRegistry.getDialectType("jdbc:oscar://h/db")).isEqualTo(DialectDbType.OSCAR);
        assertThat(DialectRegistry.getDialectType("jdbc:sybase:Tds:h:5000")).isEqualTo(DialectDbType.SYBASE);
        assertThat(DialectRegistry.getDialectType("jdbc:oceanbase://h/db")).isEqualTo(DialectDbType.OCEAN_BASE);
        assertThat(DialectRegistry.getDialectType("jdbc:unknown://h/db")).isEqualTo(DialectDbType.OTHER);
    }

    @Test
    @DisplayName("空 URL 直接拒绝")
    void blankUrlRejected() {
        // spring 的 Assert.state 抛 IllegalStateException，不是 IllegalArgumentException。
        assertThatThrownBy(() -> DialectRegistry.getDialectType(" ")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("按枚举取方言，子类型复用父方言，未支持类型返回 null")
    void dialectForType() {
        assertThat(DialectRegistry.getDialect(DialectDbType.MYSQL)).isInstanceOf(MySqlDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.CLICK_HOUSE)).isInstanceOf(MySqlDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.H2)).isInstanceOf(PostgreDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.KINGBASE_ES)).isInstanceOf(PostgreDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.DM)).isInstanceOf(OracleDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.DB2)).isInstanceOf(DB2Dialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.SQL_SERVER)).isInstanceOf(SQLServerDialect.class);
        assertThat(DialectRegistry.getDialect(DialectDbType.SYBASE)).isNull();
        assertThat(DialectRegistry.getDialects()).isNotEmpty().doesNotContainNull();
    }

    @Test
    @DisplayName("从 DataSource 元数据推断方言，取连接失败时包装为 IllegalArgumentException")
    void dialectForDataSource() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://h:3306/db");

        assertThat(DialectRegistry.getDialect(dataSource)).isInstanceOf(MySqlDialect.class);
        assertThat(DialectRegistry.getDialectType(dataSource)).isEqualTo(DialectDbType.MYSQL);

        DataSource broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("boom"));
        assertThatThrownBy(() -> DialectRegistry.getDialectType(broken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("MySQL：offset 为 0 时只有一个占位符，否则追加 offset")
    void mysqlPagination() {
        Dialect dialect = new MySqlDialect();

        DialectModel onlyLimit = dialect.paginationSql("select * from t", 0, 10);
        assertThat(onlyLimit.getDialectSql()).isEqualTo("select * from t LIMIT ?");
        assertThat(consumers(onlyLimit, 10L, 0L)).containsEntry("swak_first", 10L);

        DialectModel withOffset = dialect.paginationSql("select * from t", 5, 10);
        assertThat(withOffset.getDialectSql()).isEqualTo("select * from t LIMIT ?,?");
        assertThat(consumers(withOffset, 5L, 10L))
                .containsEntry("swak_first", 5L)
                .containsEntry("swak_second", 10L);
    }

    @Test
    @DisplayName("PostgreSQL：offset 为 0 时省略 OFFSET 子句")
    void postgrePagination() {
        Dialect dialect = new PostgreDialect();

        assertThat(dialect.paginationSql("select 1", 0, 10).getDialectSql()).isEqualTo("select 1 LIMIT ?");
        assertThat(dialect.paginationSql("select 1", 20, 10).getDialectSql())
                .isEqualTo("select 1 LIMIT ? OFFSET ?");
    }

    @Test
    @DisplayName("Oracle：offset >= 1 时把 limit 放大成 offset + limit")
    void oraclePagination() {
        Dialect dialect = new OracleDialect();

        DialectModel model = dialect.paginationSql("select 1 from dual", 5, 10);
        assertThat(model.getDialectSql()).contains("ROWNUM <=").contains("ROW_ID >");
        Map<String, Object> params = consumers(model, 5L, 15L);
        assertThat(params).containsEntry("swak_first", 15L).containsEntry("swak_second", 5L);
    }

    @Test
    @DisplayName("SQLServer 与 DB2 使用各自的 OFFSET/FETCH 语法，DB2 去掉尾分号")
    void sqlServerAndDb2Pagination() {
        Dialect sqlServer = new SQLServerDialect();
        assertThat(sqlServer.paginationSql("select 1", 3, 4).getDialectSql())
                .isEqualTo("select 1 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        assertThat(consumers(sqlServer.paginationSql("select 1", 3, 4), 3L, 4L))
                .containsEntry("swak_first", 3L)
                .containsEntry("swak_second", 4L);

        Dialect db2 = new DB2Dialect();
        assertThat(db2.paginationSql("select 1 ;", 3, 4).getDialectSql())
                .isEqualTo("select 1 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        assertThat(db2.paginationSql(" select 1 ", 3, 4).getDialectSql())
                .isEqualTo("select 1 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    @Test
    @DisplayName("DialectModel 的 consumers 同时写入参数映射与 additionalParameters")
    void dialectModelConsumesBothSides() {
        DialectModel model = new DialectModel("select 1 limit ?, ?", 7, 9).setConsumerChain();

        List<ParameterMapping> mappings = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        model.consumers(mappings, params);

        assertThat(mappings).extracting(ParameterMapping::getPropertyName)
                .containsExactly("swak_first", "swak_second");
        assertThat(mappings).extracting(ParameterMapping::getIndex).containsExactly(0, 1);
        assertThat(params).containsEntry("swak_first", 7L).containsEntry("swak_second", 9L);

        // 只设置第一个值时不产生第二个参数。
        DialectModel firstOnly = new DialectModel("select 1 limit ?", 3).setConsumer(true);
        List<ParameterMapping> onlyFirst = new ArrayList<>();
        Map<String, Object> onlyFirstParams = new HashMap<>();
        firstOnly.consumers(onlyFirst, onlyFirstParams);
        assertThat(onlyFirst).hasSize(1);
        assertThat(onlyFirstParams).containsOnlyKeys("swak_first");

        DialectModel secondOnly = new DialectModel("select 1 limit ?", 0, 4).setConsumer(false);
        List<ParameterMapping> onlySecond = new ArrayList<>();
        Map<String, Object> onlySecondParams = new HashMap<>();
        secondOnly.consumers(onlySecond, onlySecondParams);
        assertThat(onlySecond).hasSize(1);
        assertThat(onlySecondParams).containsOnlyKeys("swak_second");

        assertThat(new DialectModel("select 1").getDialectSql()).isEqualTo("select 1");
    }

    private static Map<String, Object> consumers(DialectModel model, long first, long second) {
        List<ParameterMapping> mappings = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        model.consumers(mappings, params);
        assertThat(mappings).isNotEmpty();
        assertThat(model.getDialectSql()).isNotBlank();
        if (first != 0L || second != 0L) {
            assertThat(params).isNotEmpty();
        }
        return params;
    }
}
