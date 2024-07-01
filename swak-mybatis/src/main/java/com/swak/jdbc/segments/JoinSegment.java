package com.swak.jdbc.segments;

public interface JoinSegment extends SqlSegment {

    JoinSegment where(SqlSegment... sqlSegment);
}
