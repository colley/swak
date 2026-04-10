package com.swak.common.dto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface BatchResponse<T> {
    List<Response<T>> getResponses();

    default BatchResponse<T> add(Response<T> response) {
        getResponses().add(response);
        return this;
    }

    default boolean anySuccess() {
        return getResponses().stream().map(item -> item.isSuccess()).findAny().orElse(false);
    }

    default boolean allSuccess() {
        List<Boolean> booleanList = getResponses().stream().map(item ->
                item.isSuccess()).collect(Collectors.toList());
        return booleanList.size() == getResponses().size();
    }

    static <T> BatchResponse<T> build() {
        return new _BatchResponse();
    }

    static <T> BatchResponse<T> empty() {
        return new _BatchResponse();
    }
}
