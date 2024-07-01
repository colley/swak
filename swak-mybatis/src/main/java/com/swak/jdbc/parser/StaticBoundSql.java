
package com.swak.jdbc.parser;


import com.alibaba.fastjson2.JSON;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class StaticBoundSql implements SwakBoundSql {

    @Setter
    protected String sql;

    @Setter
    protected List<ParameterMapping> parameterMappings;

    @Setter
    protected  Map<String, Object> additionalParameter;


    public StaticBoundSql(){

    }

    public StaticBoundSql(String sql,List<ParameterMapping> parameterMappings,Map<String, Object> additionalParameter){
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.additionalParameter = additionalParameter;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public Object[] getParamObjectValues() {
        if(CollectionUtils.isEmpty(parameterMappings)){
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        ParameterMapping[] mappingArray = parameterMappings.toArray(new ParameterMapping[0]);
        MapDataExchange dataExchange = new MapDataExchange();
        return dataExchange.getData(mappingArray, additionalParameter);
    }

    @Override
    public Map<String, Object> getAdditionalParameters() {
        return additionalParameter;
    }

    @Override
    public List<ParameterMapping> getParameterMapping() {
        return parameterMappings;
    }


    public static void main(String[] args) {
        String sql = "UPDATE CHANNEL_ACCOUNT_REPORT SET name=#{name},age=#{age},createTime=#{date} WHERE name=#{name3}";
        Map<String, Object> paramObj = JSON.parseObject("{\"name3\":\"张三\",\"name4\":\"张三1\",\"age\":11,\"name\":\"colley\"}");
        HsSqlSourceBuilder sqlSourceBuilder = new HsSqlSourceBuilder();
        HsSqlSource hsSqlSource = sqlSourceBuilder.parseProviderSql(sql);
        paramObj.put("date",BigInteger.valueOf(1L));
        SwakBoundSql boundSql = hsSqlSource.getBoundSql(paramObj);
        System.out.println(boundSql.getSql());
        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));
    }


}
