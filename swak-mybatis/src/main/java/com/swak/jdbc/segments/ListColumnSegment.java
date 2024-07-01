package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ListColumnSegment extends AbstractSqlSegment implements ColumnSegment {

    private List<ColumnSegment> columnsList = Lists.newArrayList();

    public ListColumnSegment() {
        super(SqlKeyword.APPLY);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        List<String> columnList = columnsList.stream().map(sqlSegment ->sqlSegment.getSqlSegment(valuePairs))
                .collect(Collectors.toList());
        return StringUtils.join(columnList, ",");
    }

    public static ListColumnSegment column(String... columns) {
        ListColumnSegment columnSegment = new ListColumnSegment();
        columnSegment.addColumn(columns);
        return columnSegment;
    }

    public ColumnSegment addColumn(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            for (String column : columns)
                columnsList.add(AliasColumnSegment.as(column));
        }
        return this;
    }
}
