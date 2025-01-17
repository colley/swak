package com.swak.formula.entity;

import com.swak.common.dto.base.DTO;
import groovy.lang.Binding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * FormulaAround.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
@Accessors(chain = true)
public class FormulaAroundCompile implements DTO {

    private Long startTime;

    private String originalScript;

    private String compileScript;

    private Map<String, String> mockData;
}
