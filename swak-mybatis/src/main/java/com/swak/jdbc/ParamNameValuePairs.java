
package com.swak.jdbc;

import com.swak.jdbc.metadata.TableList;

import java.io.Serializable;
import java.util.Map;

/**
 * ParamNameValuePairs.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface ParamNameValuePairs extends Serializable{
     Map<String, Object> getParameter();

     String addParameter(String parameterName, Object parameterValue);

     boolean containParameter(String parameterName);

     TableList getTableList();

     default  void clear() {}
}
