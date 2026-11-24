package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.MatchSegment;
import com.swak.jdbc.enums.SqlKeyword;

import java.util.List;
import java.util.stream.Collectors;

import static com.swak.common.util.StringPool.*;

public class NormalSegmentList extends AbstractSegmentList{

    /**
     * 是否处理了的上个 not
     */
    private boolean executeNot = true;

    NormalSegmentList() {
        this.flushLastValue = true;
    }

    @Override
    protected boolean transformList(List<SqlSegment> list,
                                    SqlSegment firstSegment, SqlSegment lastSegment) {
        if (list.size() == 1 && !MatchSegment.AND_OR.match(firstSegment) && !MatchSegment.NOT.match(firstSegment)) {
            /*
             * 单个条件片段（between/in/isNull/inSql/apply 等走的是「不带 APPLY 前缀」的单段注册）。
             * 这类片段既不是连接符也不是 NOT，必须当作普通条件处理，否则：
             * 1. 作为第一个条件时会被直接丢弃（整条 WHERE 消失）；
             * 2. 跟在其他条件后面时不会补 AND，拼出 `a = 1 b = 2` 这样的非法 SQL。
             */
            if (!MatchSegment.AND_OR.match(lastValue) && !isEmpty()) {
                add(SqlKeyword.AND);
            }
            if (!executeNot) {
                list.add(0, SqlKeyword.NOT);
                executeNot = true;
            }
            return true;
        }
        if (list.size() == 1) {
            /* 只有 and() 以及 or() 以及 not() 会进入 */
            if (!MatchSegment.NOT.match(firstSegment)) {
                if (isEmpty()) {
                    //sqlSegment是 and 或者 or 并且在第一位,不继续执行
                    return false;
                }
                boolean matchLastAnd = MatchSegment.AND.match(lastValue);
                boolean matchLastOr = MatchSegment.OR.match(lastValue);
                if (matchLastAnd || matchLastOr) {
                    //上次最后一个值是 and 或者 or
                    if (matchLastAnd && MatchSegment.AND.match(firstSegment)) {
                        return false;
                    } else if (matchLastOr && MatchSegment.OR.match(firstSegment)) {
                        return false;
                    } else {
                        //和上次的不一样
                        removeAndFlushLast();
                    }
                }
            } else {
                executeNot = false;
                return false;
            }
        } else {
            if (MatchSegment.APPLY.match(firstSegment)) {
                list.remove(0);
            }
            if (!MatchSegment.AND_OR.match(lastValue) && !isEmpty()) {
                add(SqlKeyword.AND);
            }
            if (!executeNot) {
                list.add(0, SqlKeyword.NOT);
                executeNot = true;
            }
        }
        return true;
    }

    @Override
    protected String childrenSqlSegment(ParamNameValuePairs valuePairs) {
        if (MatchSegment.AND_OR.match(lastValue)) {
            removeAndFlushLast();
        }
        final String str = this.stream().map(sqlSegment->sqlSegment.getSqlSegment(valuePairs))
                .collect(Collectors.joining(SPACE));
        return ( OPEN_PARENTHESIS+ str + CLOSE_PARENTHESIS);
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }
}
