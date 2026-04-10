package com.swak.formula.executor;

/**
 * @author colley
 */
public interface FormulaAutoCloseable extends  AutoCloseable{

    @Override
    default  void close() throws Exception {}
}
