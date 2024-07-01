package com.swak.jdbc.conditions;

import com.swak.jdbc.enums.SqlKeyword;


public interface StringJoin<Children, T> {

    /**
     * left join
     */
    default Children leftJoin(String joinSql) {
        return leftJoin(true, joinSql);
    }

    default Children leftJoin(boolean condition, String joinSql) {
        return join(SqlKeyword.LEFT_JOIN, condition, joinSql);
    }


        /**
         * right join
         */
        default Children rightJoin(String joinSql) {
            return rightJoin(true, joinSql);
        }

        default Children rightJoin(boolean condition, String joinSql) {
            return join(SqlKeyword.RIGHT_JOIN, condition, joinSql);
    }


    /**
     * inner join
     */
    default Children innerJoin(String joinSql) {
        return innerJoin(true, joinSql);
    }

    default Children innerJoin(boolean condition, String joinSql) {
        return join(SqlKeyword.INNER_JOIN, condition, joinSql);
    }

    /**
     * full join
     */
    default Children fullJoin(String joinSql) {
        return fullJoin(true, joinSql);
    }

    default Children fullJoin(boolean condition, String joinSql) {
        return join(SqlKeyword.FULL_JOIN, condition, joinSql);
}

    Children join(SqlKeyword keyWord, boolean condition, String joinSql);
}
