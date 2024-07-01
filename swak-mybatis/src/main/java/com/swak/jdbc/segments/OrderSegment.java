package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

/**
 * <p>
 *   order by name DESC
 *   order by name ASC
 * </p>
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:19
 **/
public class OrderSegment extends AbstractSqlSegment {

    public OrderSegment(String property, boolean ascending) {
        super(property, ascending ? SqlKeyword.ASC : SqlKeyword.DESC);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return getProperty() + ' ' + getSqlKeyword().getSqlSegment(valuePairs);
    }

    @Override
    public String toString() {
        return getProperty() + ' ' + getSqlKeyword().getKeyword();
    }

    /**
     * Ascending order
     */
    public static OrderSegment asc(String property) {
        return new OrderSegment(property, true);
    }

    /**
     * Descending order
     */
    public static OrderSegment desc(String property) {
        return new OrderSegment(property, false);
    }

    public static OrderSegment fuzzy(String property, String order) {
        boolean ascending = !"desc".equalsIgnoreCase(order);
        return new OrderSegment(property, ascending);
    }

    public static OrderSegment order(String property, boolean ascending) {
        return new OrderSegment(property, ascending);
    }
}
