/**
 * Copyright © 2022 SWAK Info.
 * File Name: SwakClosure.java
 */
package com.swak.core.command.closure;

import java.lang.reflect.Method;

/**
 * @author colley.ma
 * @since  2022/01/20
 */
public class SwakClosure {

    private final Method closureMethod;
    private final Object closureObj;

    public SwakClosure() {
        closureMethod = null;
        closureObj = null;
    }

    public SwakClosure(Method closureMethod, Object closureObj) {
        this.closureMethod = closureMethod;
        this.closureObj = closureObj;
    }

    public Method getClosureMethod() {
        return closureMethod;
    }

    public Object getClosureObj() {
        return closureObj;
    }
}
