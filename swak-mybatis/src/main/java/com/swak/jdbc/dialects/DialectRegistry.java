package com.swak.jdbc.dialects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:58
 */
@Slf4j
public class DialectRegistry {

    private static final Map<DialectDbType, Dialect> DIALECT_ENUM_MAP = new EnumMap<>(DialectDbType.class);

    static  {
        // mysql and children
        DIALECT_ENUM_MAP.put(DialectDbType.MYSQL, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.MARIADB, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.GBASE, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.OSCAR, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.XU_GU, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.CLICK_HOUSE, new MySqlDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.OCEAN_BASE, new MySqlDialect());
        // postgresql and children
        DIALECT_ENUM_MAP.put(DialectDbType.POSTGRE_SQL, new PostgreDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.H2, new PostgreDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.SQLITE, new PostgreDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.HSQL, new PostgreDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.KINGBASE_ES, new PostgreDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.PHOENIX, new PostgreDialect());
        // oracle and children
        DIALECT_ENUM_MAP.put(DialectDbType.ORACLE, new OracleDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.DM, new OracleDialect());
        DIALECT_ENUM_MAP.put(DialectDbType.GAUSS, new OracleDialect());
        // other
        DIALECT_ENUM_MAP.put(DialectDbType.DB2, new DB2Dialect());
        DIALECT_ENUM_MAP.put(DialectDbType.SQL_SERVER, new SQLServerDialect());
    }

    public static Dialect getDialect(DialectDbType dbType) {
        return DIALECT_ENUM_MAP.get(dbType);
    }

    public static Dialect getDialect(DataSource dataSource) {
        return getDialect(getDialectType(dataSource));
    }

    public static Collection<Dialect> getDialects() {
        return Collections.unmodifiableCollection(DIALECT_ENUM_MAP.values());
    }

    public static DialectDbType getDialectType(DataSource dataSource) {
        try {
            return getDialectType(dataSource.getConnection().getMetaData().getURL());
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
    /**
     * 根据连接地址判断数据库类型
     *
     * @param jdbcUrl 连接地址
     * @return ignore
     */
    public static DialectDbType getDialectType(String jdbcUrl) {
        Assert.state(StringUtils.isNotBlank(jdbcUrl), "Error: The jdbcUrl is Null, Cannot read database type");
        String url = jdbcUrl.toLowerCase();
        if (url.contains(":mysql:") || url.contains(":cobar:")) {
            return DialectDbType.MYSQL;
        } else if (url.contains(":mariadb:")) {
            return DialectDbType.MARIADB;
        } else if (url.contains(":oracle:")) {
            return DialectDbType.ORACLE;
        }else if (url.contains(":sqlserver2012:")) {
            return DialectDbType.SQL_SERVER;
        } else if (url.contains(":postgresql:")) {
            return DialectDbType.POSTGRE_SQL;
        } else if (url.contains(":hsqldb:")) {
            return DialectDbType.HSQL;
        } else if (url.contains(":db2:")) {
            return DialectDbType.DB2;
        } else if (url.contains(":sqlite:")) {
            return DialectDbType.SQLITE;
        } else if (url.contains(":h2:")) {
            return DialectDbType.H2;
        } else if (regexFind(":dm\\d*:", url)) {
            return DialectDbType.DM;
        } else if (url.contains(":xugu:")) {
            return DialectDbType.XU_GU;
        } else if (regexFind(":kingbase\\d*:", url)) {
            return DialectDbType.KINGBASE_ES;
        } else if (url.contains(":phoenix:")) {
            return DialectDbType.PHOENIX;
        } else if (jdbcUrl.contains(":zenith:")) {
            return DialectDbType.GAUSS;
        } else if (jdbcUrl.contains(":gbase:")) {
            return DialectDbType.GBASE;
        } else if (jdbcUrl.contains(":clickhouse:")) {
            return DialectDbType.CLICK_HOUSE;
        } else if (jdbcUrl.contains(":oscar:")) {
            return DialectDbType.OSCAR;
        } else if (jdbcUrl.contains(":sybase:")) {
            return DialectDbType.SYBASE;
        } else if (jdbcUrl.contains(":oceanbase:")) {
            return DialectDbType.OCEAN_BASE;
        } else {
            log.warn("The jdbcUrl is " + jdbcUrl + ", Cannot Read Database type or The Database's Not Supported!");
            return DialectDbType.OTHER;
        }
    }

    /**
     * 正则匹配
     *
     * @param regex 正则
     * @param input 字符串
     * @return 验证成功返回 true，验证失败返回 false
     */
    public static boolean regexFind(String regex, CharSequence input) {
        if (null == input) {
            return false;
        }
        return Pattern.compile(regex).matcher(input).find();
    }
}
