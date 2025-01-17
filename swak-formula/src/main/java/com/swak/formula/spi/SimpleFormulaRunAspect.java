package com.swak.formula.spi;

import com.alibaba.fastjson2.JSON;
import com.swak.common.exception.SwakExceptionUtil;
import com.swak.formula.entity.FormulaAround;
import com.swak.formula.entity.FormulaVariable;
import lombok.extern.slf4j.Slf4j;

/**
 * SimpleFormulaAroundAspect.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class SimpleFormulaRunAspect implements FormulaRunAspect {

    @Override
    public void afterProcess(FormulaVariable variableInput,FormulaAround formula) {
        if (log.isDebugEnabled()) {
            log.debug("FormulaExecutor(scriptText={},params={}) elapsed: {} ms", formula.getScript(),
                    JSON.toJSONString(formula.getBinding().getVariables()),
                    System.currentTimeMillis() - formula.getStartTime());
        }
    }

    @Override
    public void onError(FormulaVariable variableInput,FormulaAround formula, Exception e) {
        log.error("FormulaExecutor(scriptText={},params={}),message:{}", formula.getScript(),
                JSON.toJSONString(formula.getBinding().getVariables()), SwakExceptionUtil.getExceptionStackTrace(e));
    }

    @Override
    public void onSuccess(FormulaVariable variableInput,FormulaAround formula) {
        log.info(JSON.toJSONString(formula));
    }

    @Override
    public int priority() {
        return SPI_PRIORITY * 10;
    }
}
