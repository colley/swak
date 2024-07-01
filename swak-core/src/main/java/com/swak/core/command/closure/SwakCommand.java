
package com.swak.core.command.closure;

/**
 * SwakCommand.java
 */
public interface SwakCommand<T> {

    /**
     * Process logic.
     */
    T invoke();
}
