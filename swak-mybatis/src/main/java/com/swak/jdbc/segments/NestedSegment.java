package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

import java.util.Objects;
import java.util.function.Function;

/**
 * 渲染时才能确定内容的片段。
 * <p>
 * 嵌套条件（{@code nested}/{@code and}/{@code or}/{@code not}）需要把它们「条件部分」的 SQL 拼进父级，
 * 而子 wrapper 的 {@code getSqlSegment(ParamNameValuePairs)} 返回的是整条查询语句（含 SELECT/FROM），
 * 直接当作片段使用会拼出 `AND SELECT ... FROM ...` 这种非法 SQL。本片段把渲染延迟到真正拼接时，
 * 并只取子 wrapper 的条件部分。
 *
 * @author colley.ma
 */
public class NestedSegment implements SqlSegment {

    private final Function<ParamNameValuePairs, String> renderer;

    public NestedSegment(Function<ParamNameValuePairs, String> renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return renderer.apply(valuePairs);
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }
}
