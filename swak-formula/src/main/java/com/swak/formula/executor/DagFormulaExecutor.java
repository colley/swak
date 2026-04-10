package com.swak.formula.executor;


import com.swak.formula.DagExpressionRunner;
import com.swak.formula.DagExpressionRunnerImpl;
import com.swak.formula.dag.FormulaData;

import java.util.List;

public class DagFormulaExecutor extends BaseFormulaExecutor {

    private DagExpressionRunner dagExpressionRunner;

    public DagFormulaExecutor() {
        super();
        this.dagExpressionRunner = new DagExpressionRunnerImpl();
        super.apply(this.dagExpressionRunner);
    }

    public void run(List<FormulaData> formulaDataList) {
        dagExpressionRunner.run(formulaDataList);
    }
}
