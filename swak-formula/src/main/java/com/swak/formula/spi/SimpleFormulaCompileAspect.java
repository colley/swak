package com.swak.formula.spi;

import com.swak.common.exception.SwakExceptionUtil;
import com.swak.formula.entity.FormulaAroundCompile;
import lombok.extern.slf4j.Slf4j;

/**
 * SimpleFormulaAroundAspect.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class SimpleFormulaCompileAspect implements FormulaCompileAspect {

    @Override
    public void beforeCompile(FormulaAroundCompile compile) {
        FormulaCompileAspect.super.beforeCompile(compile);
    }

    @Override
    public void afterCompile(FormulaAroundCompile compile) {
        if (log.isDebugEnabled()) {
            log.debug("FormulaCompile(originalScript={}) elapsed: {} ms", compile.getCompileScript(),
                    System.currentTimeMillis() - compile.getStartTime());
        }
    }

    @Override
    public void onError(FormulaAroundCompile compile, Exception e) {
        log.error("FormulaCompile(originalScript={}),message:{}",compile.getOriginalScript(),
                SwakExceptionUtil.getExceptionStackTrace(e));
    }

    @Override
    public int priority() {
        return SPI_PRIORITY * 10;
    }
}
