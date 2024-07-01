package com.swak.jdbc.segments;


import com.swak.common.util.StringPool;

public interface ColumnSegment<R> extends SqlSegment {

     default String getTableAlias() {
          return StringPool.EMPTY;
     }
}
