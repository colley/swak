package com.swak.jdbc.segments;


import com.swak.jdbc.conditions.*;


public interface WhereSegment<Children> extends Compare<Children>
        ,Func<Children>,Nested<Children, Children>, Join<Children>, SqlSegment {

    Children where(SqlSegment... sqlSegment);
}
