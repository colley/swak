package com.swak.formula.entity;

import com.swak.common.dto.base.DTO;

import java.util.Map;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface FormulaVariable extends DTO {

    StateContext getStateContext();

    Object getVariable(String name);

    Object getResult(String name);

    Map<Object, Object> getResult();

    void binding(Map<String, Object> result);

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    void setVariable(String name, Object value);

    /**
     * remove the variable with the specified name
     *
     * @param name the name of the variable to remove
     */
    void removeVariable(String name);

    /**
     * Simple check for whether the binding contains a particular variable or not.
     *
     * @param name the name of the variable to check for
     */
    boolean hasVariable(String name);

    Map<String, Object> getVariables();

}
