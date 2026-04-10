package com.swak.formula.entity;

import com.swak.common.dto.base.DTO;
import groovy.lang.Binding;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * FormulaAround.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
@Accessors(chain = true)
public class FormulaAround implements DTO {

    private Long startTime;

    private String script;

    private VariableContext variableContext;

    private Binding binding;

    private Object result;
}
