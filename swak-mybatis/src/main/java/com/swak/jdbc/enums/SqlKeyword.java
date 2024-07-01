package  com.swak.jdbc.enums;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.segments.SqlSegment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SqlKeyword implements SqlSegment {
    AND("AND"),
    OR("OR"),
    NOT(" NOT "),
    IN(" IN"),
    NOT_IN(" NOT IN"),
    LIKE(" LIKE "),
    NOT_LIKE(" NOT LIKE "),
    EQ(StringPool.EQUAL),
    NE("<>"),
    GT(StringPool.RIGHT_CHEV),
    GE(">="),
    LT(StringPool.LEFT_CHEV),
    LE("<="),
    IS_NULL(" IS NULL"),
    IS_NOT_NULL(" IS NOT NULL"),
    GROUP_BY(" GROUP BY "),
    HAVING(" HAVING "),
    ORDER_BY(" ORDER BY "),
    EXISTS(" EXISTS "),
    NOT_EXISTS(" NOT EXISTS "),
    BETWEEN(" BETWEEN "),
    NOT_BETWEEN(" NOT BETWEEN "),
    ASC(" ASC"),
    DESC(" DESC"),
    WHEN(" WHEN "),
    LIMIT(" LIMIT "),
    SELECT(" SELECT "),
    FROM(" FROM "),
    WHERE(" WHERE "),
    LEFT_JOIN(" LEFT JOIN "),
    RIGHT_JOIN(" RIGHT JOIN "),
    INNER_JOIN(" INNER JOIN "),
    FULL_JOIN(" FULL JOIN "),
    JOIN(" JOIN "),
    TABLE(""),
    APPLY(StringPool.EMPTY),
    AS(" AS "),
    ON(" ON ");

    @Getter
    private final String keyword;

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        return this.keyword;
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return this;
    }

    @Override
    public String toString() {
        return this.keyword;
    }
}