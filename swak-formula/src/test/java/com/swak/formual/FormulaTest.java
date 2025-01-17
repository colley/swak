package com.swak.formual;


import com.alibaba.fastjson2.JSON;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.entity.FormulaVariable;
import com.swak.formula.entity.VariableContext;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.executor.FormulaExecutor;
import com.swak.formula.plugin.CircularFunctionPlugin;
import com.swak.formula.plugin.MathFunctionPlugin;
import com.swak.formula.plugin.StringFunctionPlugin;
import com.swak.formula.reflect.FormulaPluginRegistry;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 公式测试
 *
 * @since 1.0
 */

public class FormulaTest {

    private static final Logger LOG = LoggerFactory.getLogger(FormulaTest.class);

    final ClassLoader cl = GroovyShell.class.getClassLoader();
    final ScriptEngineManager factory = new ScriptEngineManager(cl);
    final ScriptEngine engine = factory.getEngineByName("groovy");
    final Compilable compilable = (Compilable) engine;

    @Before
    public void init() {
        FormulaPluginRegistry.getInstance().installPluginRegistry(
                CircularFunctionPlugin.class,
                MathFunctionPlugin.class,
                StringFunctionPlugin.class
        );
        System.out.println(JSON.toJSONString(  FormulaPluginRegistry.getInstance().getAllMetaMethods()));
    }


    @Test
    public void testThreadPools() throws FormulaCompileException {
        Map<String,Object> systemParams = new HashMap<>();
        systemParams.put("quantity",10);
        FormulaVariable variableContext = new VariableContext(systemParams);
        variableContext.setVariable("price", 12.89d);
        String input = "String heightStr = #quantity";
        FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile(input);
        System.out.println(formulaExpression);
       Object result = FormulaExecutor.getInstance().execute(formulaExpression, variableContext);
        System.out.println(result);
        systemParams.forEach((k,v)-> System.out.println(k+"="+variableContext.getVariable(k)));
    }

    @Test
    public void testCompileScript() throws FormulaCompileException {
        VariableContext variableContext = new VariableContext();
        variableContext.setVariable("quantity", 100);
        variableContext.setVariable("price", 12.89d);
        String input = "5-3";
        FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile(input);
        BigDecimal result = FormulaExecutor.getInstance().execute(formulaExpression, variableContext, BigDecimal.class);
        System.out.println(result);
    }

    @Test
    public void testScript() throws FormulaCompileException {
        VariableContext VariableContext = new VariableContext();
        VariableContext.setVariable("value", -2);
        String input = " a = Math.min(2,3); if(a>2){\n" +
                " println(\"good\");" +
                "} else {\n" +
                "println(\"heelo\");}" +
                " return abs(value);";
        String[] split = StringUtils.split(input, ";");
        Arrays.stream(split).forEach(item -> System.out.println(item));

        FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile(input);
        Object result = FormulaExecutor.getInstance().execute(formulaExpression, VariableContext);
        System.out.println(result);
    }

    @Test
    public void abs() {
        VariableContext VariableContext = new VariableContext();
        VariableContext.setVariable("value", -2);
        Object result = FormulaExecutor.getInstance().execute("abs(value)", VariableContext);
        System.out.println(result);
    }

    @Test
    public void sin() {
        VariableContext VariableContext = new VariableContext();
        VariableContext.setVariable("value", 30);
        Object result = FormulaExecutor.getInstance().execute("param = sin(value);abs(param);", VariableContext);
        System.out.println(result);
    }

    @Test
    public void shell() {
        String scriptText = "@value = 123;@param = avg(@value);if(@param>1) return true; else return false;";
        scriptText = scriptText.replaceAll("@", "");

        for (int i = 0; i < 10; i++) {
            Object result = FormulaExecutor.getInstance().execute(scriptText, null);
            System.out.println(result);
        }
    }

    @Test
    public void avg() throws FormulaCompileException {
        for (int i = 0; i < 10; i++) {
            VariableContext variableContext = new VariableContext();
            Number[] v = new Double[]{i * 1.0, 2.0};
            variableContext.setVariable("value", v);
            FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile("a = avg(value);println(a);");

            Object result = FormulaExecutor.getInstance().execute(formulaExpression, variableContext);
            System.out.println(result);

            variableContext.setVariable("value", new Double[]{5.9, i * 2.0});
            result = FormulaExecutor.getInstance().execute(formulaExpression, variableContext);
            System.out.println(result);
        }
    }

    @Test
    public void mod() throws FormulaCompileException {
        VariableContext VariableContext = new VariableContext();
        VariableContext.setVariable("NGCOUNT-2", 10);
        VariableContext.setVariable("NGCOUNT", 3);
        FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile("mod(NGCOUNT-2,NGCOUNT)");
        Object result = FormulaExecutor.getInstance().execute("mod(NGCOUNT-2,NGCOUNT)", VariableContext);
        System.out.println(result);
    }

    @Test
    public void all() {
        for (int i = 0; i < 10; i++) {
            VariableContext VariableContext = new VariableContext();
            VariableContext.setVariable("a", 30.0 * i);
            VariableContext.setVariable("b", "2.0,3.0");
            VariableContext.setVariable("value", 10.6 * i);
            Object result = FormulaExecutor.getInstance().execute("param =sin(a)", VariableContext);
            System.out.println(result);
            result = FormulaExecutor.getInstance().execute("castToInt(value)", VariableContext);
            System.out.println("castToInt(value)=" + result);

            VariableContext.setVariable("P1", "12873681236.129387");
            result = FormulaExecutor.getInstance().execute("isNumeric(P1)", VariableContext);
            System.out.println("isNumeric(P1)=" + result);
        }
    }


    @Test
    public void runJava() {
        VariableContext VariableContext = new VariableContext();
        VariableContext.setVariable("value", new Date());
        Object result = FormulaExecutor.getInstance().execute("import java.text.SimpleDateFormat;\n" +
                "SimpleDateFormat format = new SimpleDateFormat(\"yyyy年MM月dd日\");\n" +
                // 这里通过上下文传入value为当前时间，代码块中可直接使用
                // 最后一行的结果做为返回值
                "format.format(value);", VariableContext);
        LOG.info("result: {}", result);
    }

    @Test
    public void check() throws FormulaCompileException {
        String expreession = "params = 123;param1 = 456; if( params > 1000 ) return 34";
        FormulaExpression formulaExpression = FormulaExecutor.getInstance().compile(expreession);
        Object obj = FormulaExecutor.getInstance().execute(formulaExpression, null);
        System.out.println(obj);
    }

    @Test
    public void grammarCheck() {
        GroovyShell groovyShell = new GroovyShell();
        try {
            String script = "a= 123 +null";
            groovyShell.parse(script);
            //编译成脚本对象
            compilable.compile(script);
        } catch (MultipleCompilationErrorsException cfe) {
            ErrorCollector errorCollector = cfe.getErrorCollector();
            System.out.println("Errors: " + errorCollector.getErrorCount());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getEventParam() {
        VariableContext variableContext = new VariableContext();
        Map<String, Object> context = new HashMap<>();
        context.put("colley", "233");
        variableContext.addStateContext(context);
        Object result = FormulaExecutor.getInstance().execute("getEventParam('333',colley)", variableContext);
        System.out.println(result);
    }


    @Test
    public void getParamValue() {
        VariableContext variableContext = new VariableContext();
        String script = "a=true;if(a==true){b='正确'}else {b='false'}";
        Object result = FormulaExecutor.getInstance().execute(script, variableContext);
        System.out.println(result);

    }

}
