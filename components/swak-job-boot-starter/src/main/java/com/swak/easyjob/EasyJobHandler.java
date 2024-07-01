package com.swak.easyjob;


import com.swak.common.dto.Response;

/**
 * The interface Easy job handler.
 *
 * @author colley
 */
public interface EasyJobHandler {

    /**
     * JOB执行
     *
     * @param context the context
     * @return response
     */
    Response<Void> execute(EasyJobContext context);
}
