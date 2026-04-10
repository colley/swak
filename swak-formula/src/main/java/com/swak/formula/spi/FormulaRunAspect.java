package com.swak.formula.spi;

import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import com.swak.formula.entity.FormulaAround;
import com.swak.formula.entity.FormulaVariable;
import com.swak.formula.entity.VariableContext;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface FormulaRunAspect extends SpiPriority {

    default void beforeProcess(FormulaVariable variableInput, FormulaAround formula) {
        formula.setStartTime(System.currentTimeMillis());
    }

    default void afterProcess(FormulaVariable variableInput,FormulaAround formula) {
    }

    default void onSuccess(FormulaVariable variableInput,FormulaAround formula) {}


    default void onError(FormulaVariable variableInput,FormulaAround formula, Exception e) {

    }

    static FormulaRunAspect getFormulaRunAspect() {
        return SpiServiceFactory.loadFirst(FormulaRunAspect.class);
    }
}
