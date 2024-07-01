package com.swak.jdbc.parser;

import com.swak.common.util.BiIntFunction;
import com.swak.common.util.StringEscape;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ProviderBoundSql
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 11:22
 **/
public class ProviderBoundSql extends StaticBoundSql {
    private static final Pattern MP_SQL_PLACE_HOLDER = Pattern.compile("[{](?<idx>\\d+)}");
    private static ConversionService conversionService = DefaultConversionService.getSharedInstance();

    @Override
    public String getSql() {

        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        return sqlArgsFill(sql, getParamObjectValues());
    }

    private String sqlParam(Object obj) {
        String repStr;
        if (obj instanceof Collection) {
            repStr = quotaMarkList((Collection) obj);
        } else {
            repStr = quotaMark(obj);
        }
        return repStr;
    }

    private String quotaMark(Object obj) {
        Object srcObj = obj;
        if (Objects.nonNull(obj) && obj instanceof Date) {
            srcObj = new Timestamp(((Date) obj).getTime());
        }
        String srcStr = conversionService.convert(srcObj, String.class);
        if (srcObj instanceof Number) {
            return srcStr;
        }
        return StringEscape.escapeString(srcStr);
    }

    private String quotaMarkList(Collection<?> coll) {
        return coll.stream().map(this::quotaMark)
                .collect(Collectors.joining(",", "(", ")"));
    }

    private String sqlArgsFill(String content, Object... args) {
        if (StringUtils.isNotBlank(content) && ArrayUtils.isNotEmpty(args)) {
            BiIntFunction<Matcher, CharSequence> handler = (m, i) ->
                    sqlParam(args[Integer.parseInt(m.group("idx"))]);
            return replace(content, MP_SQL_PLACE_HOLDER, handler).toString();
        } else {
            return content;
        }
    }

    private StringBuilder replace(CharSequence src, Pattern ptn,
                                  BiIntFunction<Matcher, CharSequence> replacer) {
        int idx = 0;
        int last = 0;
        int len = src.length();
        Matcher m = ptn.matcher(src);
        StringBuilder sb;
        for (sb = new StringBuilder(); m.find(); last = m.end()) {
            sb.append(src, last, m.start()).append(replacer.apply(m, idx++));
        }
        if (last < len) {
            sb.append(src, last, len);
        }
        return sb;
    }
}
