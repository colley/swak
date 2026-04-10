package com.swak.formula;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.swak.formula.dag.Dag;
import com.swak.formula.dag.FormulaData;
import com.swak.formula.dag.FormulaTask;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.exception.FunctionTaskException;
import groovy.lang.Binding;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * DAG公式计算引擎
 *
 * @author colley
 * @since 1.0
 */
public final class DagExpressionRunnerImpl extends ExpressionRunnerImpl implements DagExpressionRunner {


    public DagExpressionRunnerImpl() {
        super();
    }

    public DagExpressionRunnerImpl(CompilerConfiguration configuration) {
        super(configuration);
    }

    /**
     * 公式计算
     *
     * @param formulaDataList 批量计算数据
     */
    @Override
    public void run(List<FormulaData> formulaDataList) {
        // 绑定的数据上下文
        Binding binding = new Binding();
        // 图
        MutableGraph<FormulaTask> graph = GraphBuilder.directed().build();
        List<FormulaTask> tasks = formulaDataList.stream()
                .map(formulaData -> new FormulaTask(this, binding, formulaData))
                .collect(Collectors.toList());
        // tasks
        binding.setVariable(BINDING_TASKS, tasks);
        // 顶点
        tasks.forEach(graph::addNode);
        Map<String, List<FormulaTask>> taskMap = tasks.stream().collect(Collectors.groupingBy(FormulaTask::getName));
        // 边
        tasks.forEach(toTask ->
                findVariables(toTask.getFormula()).stream()
                        .map(taskMap::get)
                        .filter(Objects::nonNull)
                        .forEach(fromTasks -> fromTasks.forEach(fromTask -> {
                            // 自己引用自己
                            if (fromTask == toTask) {
                                return;
                            }
                            graph.putEdge(fromTask, toTask);
                        }))
        );

        // 图转 DAG
        Dag dag = Dag.of(graph);
        // 计算
        runTasks(dag, binding);
    }

    private void runTasks(Dag taskGraph, Binding binding) {
        while (true) {
            FormulaTask task = taskGraph.nextTask();
            if (task == null) {
                if (taskGraph.hasTasks()) {
                    continue;
                }
                return;
            }
            try {
                binding.setVariable(BINDING_CURRENT_INDEX, task.getIndex());
                task.run();
            } catch (Throwable err) {
                // 计算失败直接返回
                if (err instanceof FormulaRuntimeException) {
                    throw new FunctionTaskException(task, err.getMessage(), ((FormulaRuntimeException) err).getOriginal());
                }
                if (err instanceof FormulaCompileException) {
                    throw new FunctionTaskException(task, err.getMessage(), ((FormulaCompileException) err).getOriginal());
                }
                throw new FunctionTaskException(task, err);
            } finally {
                taskGraph.notifyDone(task);
                binding.removeVariable(BINDING_CURRENT_INDEX);
            }
        }
    }

    private static List<String> findVariables(String scriptText) {
        if (StringUtils.isEmpty(scriptText)) {
            return Collections.emptyList();
        }
        Matcher matcher = VARIABLE_REGEX.matcher(scriptText);
        List<String> variables = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            variables.add(group);
        }
        return variables;
    }

}
