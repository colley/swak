
package com.swak.jdbc.segments;


import com.swak.jdbc.ParamNameValuePairs;

import com.swak.jdbc.enums.SqlKeyword;

public class PropertySegment extends AbstractSqlSegment {

    private String firstProperty;

    private String secondProperty;

    public PropertySegment(String property, String firstProperty, SqlKeyword sqlKeyword) {
        super(property,sqlKeyword);
        this.firstProperty = firstProperty;
    }

    public PropertySegment(String property, String firstProperty, String secondProperty,
                           SqlKeyword sqlKeyword) {
        super(property,sqlKeyword);
        this.firstProperty = firstProperty;
        this.secondProperty = secondProperty;
    }

    public PropertySegment(AliasColumnSegment property, AliasColumnSegment firstProperty,
                           SqlKeyword sqlKeyword) {
        super(property.getProperty(),sqlKeyword);
        this.firstProperty = firstProperty.getProperty();
    }


    public void setProperty(String property, String firstProperty) {
        this.property = property;
        this.firstProperty = firstProperty;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        StringBuilder sql = new StringBuilder();
        if (SqlKeyword.BETWEEN.equals(getSqlKeyword())) {
            sql.append(property).append(getSqlKeyword().getSqlSegment(paramNameValuePairs)).append(firstProperty)
                    .append(SqlKeyword.AND.getSqlSegment(paramNameValuePairs))
                    .append(secondProperty);
        } else {
            sql.append(property).append(getSqlKeyword().getSqlSegment(paramNameValuePairs)).append(firstProperty);
        }
        return sql.toString();
    }
}
