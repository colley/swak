package com.swak.formula.executor;


import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;
import com.swak.formula.common.Constant;
import com.swak.formula.common.FormulaResultCode;
import com.swak.formula.entity.CompileExpression;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.entity.FormulaFunctionParam;
import com.swak.formula.entity.FormulaMetaMethod;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.reflect.FormulaPluginRegistry;
import com.swak.formula.spi.FormulaCompile;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultCompile implements FormulaCompile {
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("(#?\\w+)\\(([^)]*)\\)");


    private static final Pattern FUNCTION_PARAMETER_PATTERN =Pattern.compile("(#?\\w+)\\(([^)]*)\\)"); //Pattern.compile("\\b(\\w+)\\((.*?)\\)");

    @Override
    public CompileExpression compile(String logic, Map<String, String> mockData) throws FormulaCompileException {
        if (StringUtils.isEmpty(logic)) {
            return FormulaExpression.EMPTY;
        }
        CompileExpression compileExpression = new CompileExpression();
        StringBuilder singleExpression = new StringBuilder();
        String[] rootLogic = StringUtils.split(logic, StringPool.NEW_LINE);
        try {
            for (String nextLogic : rootLogic) {
                compile(nextLogic,compileExpression,mockData,singleExpression);
                singleExpression.append(StringPool.NEW_LINE);
            }
            String compileScript = singleExpression.toString();
            compileScript = compileScript
                    .replaceAll(Constant.VARIABLE_CURRENT, Constant.CURRENT_VAR_NAME)
                    .replaceAll(StringPool.AT, Constant.VAR_PREFIX)
                    //.replaceAll(StringPool.NEW_LINE, "")
                    .replaceAll(StringPool.HASH, "");
            compileExpression.setCompileScript(compileScript);
            compileExpression.setVariables(Maps.newHashMap());
            compileExpression.setOriginalScript(logic);
        } catch (Exception ex) {
            throw new FormulaCompileException(FormulaResultCode.FORMULA_COMPILE_ERR, ex);
        }
        return compileExpression;
    }

    private void compile(String subLogic,CompileExpression compileExpression,Map<String, String> mockData,StringBuilder singleExpression){
        String[] rootLogic = StringUtils.split(subLogic, ";");
        for (String nextLogic : rootLogic) {
            String scriptLabel = formatVariables(nextLogic, mockData, compileExpression.getRelatedFunctions());
            singleExpression.append(scriptLabel);
            if(!scriptLabel.endsWith("{")){
                singleExpression.append(";");
            }
        }
    }

    @Override
    public String formatVariables(String formulaStr, Map<String, String> formulaValue, List<FormulaFunctionParam> relatedFunctions) {
        Matcher matcher = FUNCTION_PATTERN.matcher(formulaStr);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            //getEventParameter(6549,'www')
            String functionScript = matcher.group(0);
            String funcReplacement = resolveFunctionScript(functionScript, formulaValue, relatedFunctions);
            matcher.appendReplacement(sb, "" + funcReplacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveFunctionScript(String funcStr, Map<String, String> formulaValue, List<FormulaFunctionParam> relatedFunctions) {
        FormulaFunctionParam relatedFunction = getFunctionAndParams(funcStr);
        if (Objects.isNull(relatedFunction)) {
            return funcStr;
        }
        String functionName = relatedFunction.getFunctionName();
        relatedFunctions.add(relatedFunction);
        boolean isCustomFunction = functionName.startsWith(StringPool.HASH);
        if(isCustomFunction){
            functionName = GetterUtil.firstToUpperCase(functionName.substring(1));
        }
        //增加函数名
        FormulaMetaMethod formulaMetaMethod = FormulaPluginRegistry.getInstance()
                .getCustomFormula(functionName);
        if (Objects.isNull(formulaMetaMethod)) {
            return funcStr;
        }
        List<String> functionParams = relatedFunction.getFunctionParams();
        //过滤mock数据替换
        String mockFunctionKey = getHashFunctionMockKey(relatedFunction.getFunctionName(), functionParams);
        String hashFunctionMockKey = mockFunctionKey.substring(1);
        String funcReplacement = Optional.ofNullable(formulaValue.get(mockFunctionKey)).orElse(formulaValue.get(hashFunctionMockKey));
        if (formulaValue.containsKey(mockFunctionKey) || formulaValue.containsKey(hashFunctionMockKey)) {
            //包含对应函数，函数需要替代传递的值
            if (formulaMetaMethod.isStrReturnType()) {
                return "'" + funcReplacement + "'";
            } else {
                return funcReplacement;
            }
        }
        //没参数不需要过滤
        if (CollectionUtils.isEmpty(functionParams)) {
            return funcStr;
        }
        return functionName + StringPool.OPEN_PARENTHESIS + Joiner.on(",")
                .join(functionParams) +
                StringPool.CLOSE_PARENTHESIS;
    }

    private FormulaFunctionParam getFunctionAndParams(String funcStr) {
        Matcher matcher = FUNCTION_PARAMETER_PATTERN.matcher(funcStr);

        if (matcher.find()) {
            FormulaFunctionParam relatedFunction = new FormulaFunctionParam();
            String functionName = matcher.group(1); // 获取函数名
            String parameters = matcher.group(2);   // 获取参数列表
            relatedFunction.setFunctionName(functionName);
            List<String> paramsList = new ArrayList<>();
            if (StringUtils.isNotBlank(parameters)) {
                String[] parameterSplit = StringUtils.split(parameters, ",");
                for (String parameterName : parameterSplit) {
                    paramsList.add(StringUtils.trim(parameterName));
                }
            }
            relatedFunction.setFunctionParams(paramsList);
            return relatedFunction;
        }
        return null;
    }

    private String getHashFunctionMockKey(String functionName, List<String> functionParams) {
        StringBuilder mockKeyBuf = new StringBuilder();
        mockKeyBuf.append(StringPool.HASH).append(functionName)
                .append(StringPool.OPEN_PARENTHESIS);
        if (CollectionUtils.isNotEmpty(functionParams)) {
            mockKeyBuf.append(Joiner.on(",").join(functionParams));
        }
        mockKeyBuf.append(StringPool.CLOSE_PARENTHESIS);
        return mockKeyBuf.toString();
    }
}
