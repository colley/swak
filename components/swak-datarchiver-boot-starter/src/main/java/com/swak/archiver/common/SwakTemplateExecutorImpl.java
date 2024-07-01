
package com.swak.archiver.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;


/**
 * ExecutionMarsServiceImpl.java
 *
 * @author ColleyMa
 * @version 19-5-7 下午3:59
*/

@Slf4j
public class SwakTemplateExecutorImpl implements SwakTemplateExecutor {
    /** spring事务*/
    private TransactionTemplate transactionTemplate;
    private JdbcTemplate jdbcTemplate;

    public SwakTemplateExecutorImpl(DataSource dataSource,boolean isMysql) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        if(isMysql) {
            PlatformTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource);
            this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        }
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    @Override
    public void execute(String sql) throws DataAccessException {
        jdbcTemplate.execute(sql);
    }

    /**
     * 根据where条件查询总条数
     *
     * @param sql
     * @return
     * @throws DataAccessException
     */
    @Override
    public Integer findTotalCount(String sql) throws DataAccessException {
        return jdbcTemplate.queryForObject(sql,Integer.class);
    }

    /**
     * 获取表的maxId
     *
     * @param sql
     * @return
     * @throws DataAccessException
     */
    @Override
    public Long findMaxId(String sql) throws DataAccessException {
        return jdbcTemplate.queryForObject(sql,Long.class);
    }

    @Override
    public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
        return  jdbcTemplate.queryForRowSet(sql);
    }

    /**
     * 批量删除
     *
     * @param sql
     * @return
     * @throws DataAccessException
     */
    @Override
    public int batchUpdate(String sql) throws DataAccessException {
        return jdbcTemplate.update(sql);
    }




}
