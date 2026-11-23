package com.swak.test.autoconfigure;

import com.swak.jdbc.spi.AbstractSwakJdbcTemplate;
import com.swak.jdbc.spi.SwakJdbcTemplate;
import com.swak.jdbc.spring.configuration.SwakJdbcAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SwakJdbcAutoConfiguration 依赖 DataSource 存在才装配。
 * <p>
 * 装配过程会通过 DataSource 探测方言（{@code DialectRegistry}），所以这里给 mock 的 DataSource
 * 补上 jdbc url，避免拿到 null 导致探测异常。
 */
class JdbcAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakJdbcAutoConfiguration.class));

    @Test
    @DisplayName("没有 DataSource 时不装配 SwakJdbcTemplate")
    void withoutDataSource() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SwakJdbcTemplate.class);
        });
    }

    @Test
    @DisplayName("存在 DataSource 时装配 SwakJdbcTemplate 并标记为 mysql")
    void withDataSource() throws SQLException {
        DataSource dataSource = mockDataSource("jdbc:mysql://localhost:3306/demo");

        runner.withBean(DataSource.class, () -> dataSource).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SwakJdbcTemplate.class);
            AbstractSwakJdbcTemplate template = context.getBean(AbstractSwakJdbcTemplate.class);
            assertThat(template.getName()).isEqualTo("mysql");
        });
    }

    private DataSource mockDataSource(String url) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn(url);
        return dataSource;
    }
}
