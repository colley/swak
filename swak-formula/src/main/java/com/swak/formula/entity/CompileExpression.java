package com.swak.formula.entity;

import com.google.common.collect.Lists;
import com.swak.common.dto.base.DTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Accessors(chain = true)
public class CompileExpression implements DTO {
	public static CompileExpression EMPTY = new CompileExpression();

	private Map<String, Object> variables;

	private String compileScript;

	private String originalScript;

	private List<FormulaFunctionParam> relatedFunctions =  Lists.newArrayList();

	private Set<String> inputVariables;
}
