package com.swak.demo;

import com.alibaba.fastjson2.JSON;
import com.swak.cache.queue.DelayEvent;
import com.swak.cache.queue.DelayedQueueHandler;
import com.swak.cache.spi.DelayedQueueManager;
import com.swak.common.util.GetterUtil;
import org.apache.commons.collections4.Get;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * De.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SwakCaseApplication.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class JdbcTableNameTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testJdbcTableName() throws SQLException {


        String tableName = "configuration";
        DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
        String schema = getDatabaseName(metaData.getURL());
//        System.out.println(schema);
//        ResultSet schemas = metaData.getSchemas();
//        while (schemas.next()) {
//            String schemaName = schemas.getString("TABLE_SCHEM");
//            System.out.println("Schema: " + schemaName);
//        }
//        ResultSet tables = metaData.getTables(null, schema, tableName, new String[]{"TABLE"});
//        while (tables.next()) {
//            ResultSet columns = metaData.getColumns(null, schema, tableName, null);
//            while (columns.next()) {
//                String colName = columns.getString("COLUMN_NAME");
//                String remarks = columns.getString("REMARKS");
//                String dbType = columns.getString("DATA_TYPE");
//                String typeName = columns.getString("TYPE_NAME");
//                System.out.println("colName:" + colName + ",remarks:" + remarks + ",dbType:" + dbType+",typeName:"+typeName);
//            }
//        }

        String query = "SELECT COLUMN_NAME, COLUMN_COMMENT,DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";

        jdbcTemplate.query(String.format(query,schema,tableName),(rs)->{
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnComment = rs.getString("COLUMN_COMMENT");
                String dataType = rs.getString("DATA_TYPE");
                System.out.println("Column: " + columnName + ", Comment: " + columnComment+",dataType: "+dataType);
            }
        });


    }

    public static String getDatabaseName(String jdbcUrl) {
        String databaseName;
        String lastJdbcUrl = GetterUtil.getSplitStr(jdbcUrl,"?")[0];
        databaseName = jdbcUrl.substring(lastJdbcUrl.lastIndexOf("/") + 1);
        if (databaseName.contains("?")) {
            databaseName = databaseName.substring(0, databaseName.indexOf("?"));
        }
        return databaseName;
    }

    public static void main(String[] args) {
        String jdbcUrl="jdbc:mysql://10.74.170.215:3306/fdc_dev?allowMultiQueries=true&serverTimezone=Asia/Shanghai";
        System.out.println(getDatabaseName(jdbcUrl));
    }
}
