package com.swak.formula.executor;


import com.google.common.collect.Maps;
import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.entity.SimulationResult;
import com.swak.formula.entity.VariableContext;
import com.swak.formula.entity.VirtualSimulationCmd;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.exception.FormulaRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class VirtualSimulation implements FormulaSimulation {

    @Override
    public Response<SimulationResult> simulation(VirtualSimulationCmd command) {
        SimulationResult simulationResult = new SimulationResult();
        FormulaExpression formulaExpression = null;
        try {
            VariableContext variableContext = new VariableContext();
            formulaExpression = FormulaExecutor.getInstance().compile(command.getLogicFormula(),
                    Optional.ofNullable(command.getMockData()).orElse(Maps.newHashMap()));
            Object result = FormulaExecutor.getInstance().execute(formulaExpression, variableContext);
            simulationResult.setResult(result);
            simulationResult.setScript(formulaExpression.getCompileScript());
            simulationResult.setParamValues(variableContext.getResult());
            return Response.success(simulationResult);
        } catch (FormulaCompileException compileEx) {
            simulationResult.setScript(command.getLogicFormula());
            Response<SimulationResult> response = Response.fail(compileEx.getErrCode(), compileEx.getErrMessage());
            response.setData(simulationResult);
            return response;
        } catch (FormulaRuntimeException e1) {
            simulationResult.setScript(Objects.nonNull(formulaExpression) ? formulaExpression.getCompileScript() : "");
            Response<SimulationResult> response = Response.fail(e1.getErrCode(), e1.getErrMessage());
            response.setData(simulationResult);
            return response;
        } catch (Exception ex) {
            log.error("[Swak-Formula] 脚本模拟验证失败", ex);
            simulationResult.setScript(Objects.nonNull(formulaExpression) ? formulaExpression.getCompileScript() : "");
            Response<SimulationResult> response = Response.fail(BasicErrCode.BIZ_ERROR);
            response.setData(simulationResult);
            return response;
        }
    }
}
