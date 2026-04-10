package com.swak.formula.spi;

import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import com.swak.formula.entity.FormulaAroundCompile;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface FormulaCompileAspect extends SpiPriority {

    default void beforeCompile(FormulaAroundCompile compile) {
        compile.setStartTime(System.currentTimeMillis());
    }

    default void afterCompile(FormulaAroundCompile compile) {
    }

    default void onSuccess(FormulaAroundCompile compile) {}


    default void onError(FormulaAroundCompile compile, Exception e) {

    }

    static FormulaCompileAspect getAroundAspect() {
        return SpiServiceFactory.loadFirst(FormulaCompileAspect.class);
    }
}
