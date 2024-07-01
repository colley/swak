/**
 * Copyright (C), 2011-2017
 * File Name: ExecutionMarsService.java
 * Encoding: UTF-8
 * Date: 17-8-23 下午5:19
 * History:
 */
package com.swak.archiver.common;


import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author mayuanchao
 * @version 1.0  Date: 17-8-23 下午5:19
 */
public interface SwakTemplateExecutor {
     TransactionTemplate getTransactionTemplate();

     JdbcTemplate getJdbcTemplate();

     void execute(final String sql) throws DataAccessException;

     /**
      * 根据where条件查询总条数
      *
      * @param sql
      * @return
      * @throws DataAccessException
      */
     Integer findTotalCount(String sql) throws DataAccessException;

     /**
      * 获取表的maxId
      *
      * @param toString
      * @return
      * @throws DataAccessException
      */
     Long findMaxId(String toString) throws DataAccessException;

     SqlRowSet queryForRowSet(String sql) throws DataAccessException;

     /**
      * 批量删除
      *
      * @param sql
      * @return
      * @throws DataAccessException
      */
     int batchUpdate(String sql) throws DataAccessException;

}
