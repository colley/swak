/*
 * Copyright (c) 2016-2017 by Colley
 * All rights reserved.
 */
package com.swak.jdbc;

import com.swak.jdbc.metadata.TableList;

import java.io.Serializable;
import java.util.Map;

/**
 * ParamNameValuePairs
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 14:49
 **/
public interface ParamNameValuePairs extends Serializable{
     Map<String, Object> getParameter();

     String addParameter(String parameterName, Object parameterValue);

     boolean containParameter(String parameterName);

     TableList getTableList();

     default  void clear() {}
}
