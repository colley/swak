package com.swak.core.handler;

import com.swak.common.dto.Response;

public abstract class AbstractHandler<R,I,C> implements Handler<Response<R>, I,C> {

    public Response<R> nextHandler(I command, C context, AbstractHandler... nextHandlers) {
        Response<R> response = invoke(command, context);
        if (!response.isSuccess()) {
            return response;
        }
        for (AbstractHandler<R,I,C> nextHandler : nextHandlers) {
            Response<R> nextResponse = nextHandler.nextHandle(command, context);
            if (!nextResponse.isSuccess()) {
                return nextResponse;
            }
        }
        return Response.success();
    }
}
