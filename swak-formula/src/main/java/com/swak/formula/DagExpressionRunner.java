package com.swak.formula;

import com.swak.formula.dag.FormulaData;

import java.util.List;
import java.util.regex.Pattern;

/**
 * DAG公式计算引擎
 *
 * @author colley
 * @since 1.0
 */
public interface DagExpressionRunner extends ExpressionRunner {

    /**
     * The constant VARIABLE_REGEX.
     */
    Pattern VARIABLE_REGEX = Pattern.compile("#\\{([^{}]+?)}");

    /**
     * The constant BINDING_CURRENT_INDEX.
     */
    String BINDING_CURRENT_INDEX = "_index";
    /**
     * The constant BINDING_TASKS.
     */
    String BINDING_TASKS = "_tasks";

    /**
     * 公式计算
     *
     * @param formulaDataList 批量计算数据
     */
    void run(List<FormulaData> formulaDataList);
}
