package com.swak.common.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RequestIdUtils {

    public static String getRequestId(Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return DigestUtils.md5(UUIDHexGenerator.generator());
        }
        List<Object> lastParams = Arrays.stream(params).map(item -> {
            if (Objects.isNull(item)) {
                return StringPool.EMPTY;
            }
            return item;
        }).collect(Collectors.toList());
        String requestParams = Joiner.on('|').join(lastParams);

        return DigestUtils.md5(requestParams);
    }
}
