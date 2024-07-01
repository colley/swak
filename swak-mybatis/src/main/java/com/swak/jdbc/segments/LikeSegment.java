
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.enums.SqlLikeMode;
import lombok.Getter;

/**
 * <p>
 *     name like CONCAT('%','c')
 *     name like CONCAT('c','%')
 *     name like CONCAT('%','c','%')
 *     ignoreCase
 *     lower(name) like CONCAT('%','c')
 * </p>
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:11
 **/
@Getter
public class LikeSegment extends AbstractSqlSegment {
    private final Object value;
    private final boolean ignoreCase;
    private final SqlLikeMode matchMode;

    public LikeSegment(String property, Object value) {
        this(property, value, false, SqlLikeMode.DEFAULT);
    }

    public LikeSegment(String property, Object value, SqlLikeMode matchMode) {
        this(property, value, false, matchMode);
    }

    public LikeSegment(String property, Object value, SqlKeyword sqlKeyword, SqlLikeMode matchMode) {
        super(property,sqlKeyword);
        this.value = value;
        this.matchMode =matchMode;
        this.ignoreCase = false;
    }

    public LikeSegment(String property, Object value, boolean ignoreCase, SqlLikeMode matchMode) {
        super(property,SqlKeyword.LIKE);
        this.property = property;
        this.value = value;
        this.matchMode = matchMode;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        String parameterName = paramNameValuePairs.addParameter(property, value);
        StringBuilder fragment = new StringBuilder();
        if (isIgnoreCase()) {
            fragment.append("lower(").append(getProperty()).append(")");
        } else {
            fragment.append(getProperty());
        }
        fragment.append(getSqlKeyword().getSqlSegment(paramNameValuePairs));
        if (SqlLikeMode.DEFAULT.equals(matchMode)) {
            fragment.append(IbsStringHelper.repeatParamFormat(property));
        }
        if (SqlLikeMode.LEFT.equals(matchMode)) {
            fragment.append("CONCAT('%',").append(IbsStringHelper.repeatParamFormat(parameterName)).append(")");
        }
        if (SqlLikeMode.RIGHT.equals(matchMode)) {
            fragment.append("CONCAT(").append(IbsStringHelper.repeatParamFormat(parameterName)).append(",'%')");
        }
        if (SqlLikeMode.ANYWHERE.name().equals(matchMode)) {
            fragment.append("CONCAT('%',").append(IbsStringHelper.repeatParamFormat(parameterName)).append(",'%')");
        }
        return fragment.toString();
    }

    @Override
	public String toString() {
        StringBuilder builder = new StringBuilder(property)
                .append(getSqlKeyword().getKeyword());
        if (SqlLikeMode.DEFAULT.equals(matchMode)) {
            builder.append(value.toString());
        } else if (SqlLikeMode.LEFT.equals(matchMode)) {
            builder.append("CONCAT('%',").append(value.toString()).append(")");
        } else if (SqlLikeMode.RIGHT.equals(matchMode)) {
            builder.append("CONCAT(").append(value.toString()).append(",'%')");
        } else if (SqlLikeMode.ANYWHERE.name().equals(matchMode)) {
            builder.append("CONCAT('%',").append(value.toString()).append(",'%')");
        }
        return builder.toString();
    }


}
