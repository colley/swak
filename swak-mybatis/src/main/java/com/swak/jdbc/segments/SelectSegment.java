package com.swak.jdbc.segments;


import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface SelectSegment extends SqlSegment{

    SelectSegment select(List<ColumnSegment> columns);

    default SelectSegment select(String... columns) {
        if(ArrayUtils.isNotEmpty(columns)){
            select(Arrays.stream(columns).map(AliasColumnSegment::as).collect(Collectors.toList()));
        }
        return this;
    }

    SelectSegment from(FromSegment fromSegment);

    default SelectSegment from(String tableName) {
        return from(TableFromSegment.from(tableName));
    }

    SelectSegment join(SqlSegment joinSegment);

    default SelectSegment join(String tableName, String  onClause) {
        return join(new TableJoinSegment(SqlKeyword.JOIN, tableName, new String[]{onClause}));
    }
    default SelectSegment leftJoin(String tableName, String  onClause) {
        return join(new TableJoinSegment(SqlKeyword.LEFT_JOIN, tableName, new String[]{onClause}));
    }

    default SelectSegment rightJoin(String tableName, String  onClause) {
        return join(new TableJoinSegment(SqlKeyword.RIGHT_JOIN, tableName, new String[]{onClause}));
    }

    default SelectSegment innerJoin(String tableName, String  onClause) {
        return join(new TableJoinSegment(SqlKeyword.INNER_JOIN, tableName, new String[]{onClause}));
    }
}
