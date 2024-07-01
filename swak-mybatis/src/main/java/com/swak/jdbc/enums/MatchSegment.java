
package  com.swak.jdbc.enums;


import com.swak.jdbc.segments.SqlSegment;

import java.util.function.Predicate;

/**
 * 匹配片段
 */
public enum MatchSegment {
    GROUP_BY(i -> i == SqlKeyword.GROUP_BY),
    ORDER_BY(i -> i == SqlKeyword.ORDER_BY),
    NOT(i -> i == SqlKeyword.NOT),
    AND(i -> i == SqlKeyword.AND),
    OR(i -> i == SqlKeyword.OR),
    AND_OR(i -> i == SqlKeyword.AND || i == SqlKeyword.OR),
    EXISTS(i -> i == SqlKeyword.EXISTS),
    HAVING(i -> i == SqlKeyword.HAVING),
    APPLY(i -> i == SqlKeyword.APPLY);

    private final Predicate<SqlSegment> predicate;

    MatchSegment(Predicate<SqlSegment> predicate) {
        this.predicate = predicate;
    }

    public boolean match(SqlSegment segment) {
        return getPredicate().test(segment);
    }

    protected Predicate<SqlSegment> getPredicate() {
        return predicate;
    }
}
