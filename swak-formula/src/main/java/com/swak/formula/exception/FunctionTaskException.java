package com.swak.formula.exception;

import com.swak.formula.dag.FormulaTask;

/**
 * 公式任务异常
 *
 * @author colley
 */
public class FunctionTaskException extends FormulaRuntimeException {

    private final FormulaTask functionTask;

    /**
     * Instantiates a new Function task exception.
     *
     * @param functionTask the function task
     * @param message      the message
     */
    public FunctionTaskException(FormulaTask functionTask, String message) {
        super(message);
        this.functionTask = functionTask;
    }

    /**
     * Instantiates a new Function task exception.
     *
     * @param functionTask the function task
     * @param cause        the cause
     */
    public FunctionTaskException(FormulaTask functionTask, Throwable cause) {
        super(cause.getMessage(), cause);
        this.functionTask = functionTask;
    }

    /**
     * Instantiates a new Function task exception.
     *
     * @param functionTask the function task
     * @param message      the message
     * @param cause        the cause
     */
    public FunctionTaskException(FormulaTask functionTask, String message, Throwable cause) {
        super(message, cause);
        this.functionTask = functionTask;
    }

    /**
     * Gets formula task.
     *
     * @return the formula task
     */
    public FormulaTask getFormulaTask() {
        return functionTask;
    }

}
