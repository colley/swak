package com.swak.core.handler;

import java.util.concurrent.Future;

/**
 * The interface Asyn handler.
 */
public interface AsyncHandler<R,I,C> extends Handler<Future<R>, I,C> {

}