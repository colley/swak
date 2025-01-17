package com.swak.formula.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * VariableContext.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public class VariableContext implements FormulaVariable {
    private final Map<String, Object> variables = new LinkedHashMap<>();

    private final StateContext stateContext = new StateContext();

    private final Map<Object, Object> result = new LinkedHashMap<>();


    public void addStateContext(Map<String, Object> variables) {
        if (Objects.nonNull(variables)) {
            this.stateContext.putAll(variables);
        }
    }

    public StateContext getStateContext() {
        return this.stateContext;
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public Object getResult(String name) {
        return result.get(name);
    }

    public Map<Object, Object> getResult() {
        return result;
    }


    public void binding(Map<String, Object> result) {
        Optional.ofNullable(result).ifPresent(this.result::putAll);
    }

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    public void setVariable(String name, Object value) {
        if (Objects.isNull(value)) {
            removeVariable(name);
        }else{
            variables.put(name, value);
        }
    }

    /**
     * remove the variable with the specified name
     *
     * @param name the name of the variable to remove
     */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Simple check for whether the binding contains a particular variable or not.
     *
     * @param name the name of the variable to check for
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public VariableContext() {
        this(new LinkedHashMap<>());
    }

    public VariableContext(Map<String, Object> variables) {
        if (Objects.nonNull(variables)) {
            this.variables.putAll(variables);
        }
    }
}
