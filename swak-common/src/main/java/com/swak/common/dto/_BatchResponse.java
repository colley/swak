package com.swak.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleBatchResponse.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class _BatchResponse<T> implements BatchResponse<T> {

    private List<Response<T>> responses = new ArrayList<>();

    @Override
    public List<Response<T>> getResponses() {
        return this.responses;
    }
}
