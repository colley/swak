package  com.swak.jdbc.conditions;



import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.parser.SwakBoundSql;
import com.swak.jdbc.segments.SqlSegment;

public interface SwakWrapper<T>  extends SqlSegment {

    T getEntity();

    Class<T> getEntityClass();

    String getSqlFirst();

    void clear();

    SwakBoundSql getBoundSql();

    String getStaticSql();

     ParamNameValuePairs getParamNameValuePairs();

    @Override
    default SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }
}
