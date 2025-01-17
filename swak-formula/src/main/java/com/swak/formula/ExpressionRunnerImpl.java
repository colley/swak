package com.swak.formula;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import com.swak.formula.common.FormulaResultCode;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.script.VariableFunctionScript;
import com.swak.formula.transform.MethodASTTransformation;
import com.swak.formula.transform.MethodCallCustomizer;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.transform.TypeChecked;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;
import java.util.Objects;

import static com.swak.formula.common.Constant.MAXIMUM_SIZE;

/**
 * 公式 执行引擎
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Slf4j
public class ExpressionRunnerImpl implements ExpressionRunner {

    private final CompilerConfiguration configuration;

    private final Cache<String, Class<Script>> scriptCache;

    /**
     * Instantiates a new Simple function engine.
     */
    public ExpressionRunnerImpl() {
        this(new CompilerConfiguration());
    }

    public ExpressionRunnerImpl(CompilerConfiguration configuration) {
        this.configuration = configuration;
        this.configuration.setScriptBaseClass(VariableFunctionScript.class.getName());
        this.configuration.setDebug(true);
        Map<Object,Object> map = ImmutableMap.of("extensions",MethodASTTransformation.class.getName());
        this.configuration.addCompilationCustomizers(new MethodCallCustomizer(this));
//        this.configuration.addCompilationCustomizers(new ImportCustomizer()
//                .addImports(
//                        Executor.class.getName(),
//                        Executors.class.getName(),
//                        ExecutorService.class.getName(),
//                        SpringBeanFactory.class.getName()
//                )
//        );
        this.scriptCache = Caffeine.newBuilder().maximumSize(MAXIMUM_SIZE).build();
    }

    /**
     * @param compileScript 表达式
     */
    @SuppressWarnings("unchecked")
    @Override
    public FormulaExpression parseClass(String compileScript) throws FormulaCompileException {
        Class<Script> scriptClass;
        try {
            String key = EncodingGroovyMethods.md5(compileScript);
            scriptClass = scriptCache.getIfPresent(key);
            if (Objects.isNull(scriptClass)) {
                try (GroovyClassLoader classLoader = new GroovyClassLoader(VariableFunctionScript.class.getClassLoader(),
                        configuration)) {
                    scriptClass = classLoader.parseClass(compileScript);
                }
                scriptCache.put(key, scriptClass);
            }
            FormulaExpression formulaExpression = new FormulaExpression();
            formulaExpression.setCompileClass(scriptClass);
            formulaExpression.setCompileScript(compileScript);
            return formulaExpression;
        } catch (Exception e) {
            log.error("[ExpressionRunner#parseClass] error", e);
            throw new FormulaCompileException(FormulaResultCode.FORMULA_COMPILE_ERR, e);
        }
    }

    /**
     * 运行脚本
     *
     * @param scriptText 公式脚本
     * @return 结果 object
     */
    @Override
    public <T> T run(String scriptText) {
        return run(scriptText, new Binding());
    }

    /**
     * 运行脚本
     *
     * @param scriptText 公式脚本
     * @param binding    绑定上下文
     * @return 结果 object
     */
    @Override
    public <T> T run(String scriptText, Binding binding) {
        FormulaExpression formulaExpression = this.parseClass(scriptText);
        formulaExpression.setOriginalScript(scriptText);
        return run(formulaExpression, binding);
    }


    private boolean causedByNullPointerException(Throwable e) {
        while (e != null) {
            if (e instanceof NullPointerException) {
                return true;
            } else {
                e = e.getCause();
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T run(FormulaExpression formulaExpression, Binding binding) {
        if (Objects.isNull(formulaExpression.getCompileScript())) {
            return null;
        }
        try {
            Script script = InvokerHelper.createScript(formulaExpression.getCompileClass(), binding);
            return (T) script.run();
        } catch (Exception e) {
            if (causedByNullPointerException(e)) {
                return null;
            }
            throw new FormulaRuntimeException(FormulaResultCode.FORMULA_RUN_ERR, e);
        }
    }


    @Override
    public void close() {
        scriptCache.invalidateAll();
        scriptCache.cleanUp();
    }
}
