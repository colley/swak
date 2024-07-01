package com.swak.jdbc.segments;

public interface FromSegment extends SqlSegment {

    void setAlias(String aliasTableName);

    String getAlias();
}
