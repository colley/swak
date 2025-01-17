package com.swak.formula.reflect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swak.common.spi.SpiServiceFactory;
import com.swak.common.util.ClassUtils;
import com.swak.common.util.GetterUtil;
import com.swak.formula.annotation.FormulaNamed;
import com.swak.formula.entity.FormulaMetaMethod;
import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.spi.FormulaPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.*;

/**
 * FormulaPluginRegistry.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class FormulaPluginRegistry {
    private final Map<String, MethodInvocation> PLUGIN_METHOD_MAPPING = Maps.newConcurrentMap();
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Map<Class<? extends FormulaPlugin>, List<FormulaMetaMethod>> FUNCTION_CACHE = Maps.newConcurrentMap();

    public Map<String, FormulaMetaMethod> FUNCTION_RETURN_TYPE = Maps.newConcurrentMap();

    public Map<String, Class<? extends FormulaPlugin>> FORMULA_PLUGINS = Maps.newConcurrentMap();


    @SafeVarargs
    public final synchronized void addFormulaPlugins(Class<? extends FormulaPlugin>... formulaPlugins) {
        if (ArrayUtils.isEmpty(formulaPlugins)) {
            return;
        }
        for (Class<? extends FormulaPlugin> formulaPlugin : formulaPlugins) {
            FORMULA_PLUGINS.putIfAbsent(formulaPlugin.getName(), formulaPlugin);
        }
    }

    @SafeVarargs
    public final synchronized void installPluginRegistry(Class<? extends FormulaPlugin>... formulaPlugins) {
        if (ArrayUtils.isNotEmpty(formulaPlugins)) {
            addFormulaPlugins(formulaPlugins);
            Arrays.stream(formulaPlugins).forEach(this::installPlugin);
        }
    }

    public synchronized void installPluginRegistry() {
        if (MapUtils.isEmpty(FUNCTION_CACHE)) {
            FORMULA_PLUGINS.values().forEach(this::installPlugin);
            this.installPluginBySpi();
        } else {
            log.warn("[Swak-Formula] The function library has been loaded");
        }
    }

    private void installPluginBySpi() {
        log.info("[Swak-Formula] loading service provider interface function.");
        List<FormulaPlugin> formulaPlugins = SpiServiceFactory.load(FormulaPlugin.class);
        if (CollectionUtils.isNotEmpty(formulaPlugins)) {
            for (FormulaPlugin formulaPlugin : formulaPlugins) {
                installPlugin(formulaPlugin.getClass());
            }
        }
    }

    private void installPlugin(Class<? extends FormulaPlugin> pluginClazz) {
        if (FUNCTION_CACHE.containsKey(pluginClazz)) {
            return;
        }
        try {
            installPlugin(ClassUtils.newInstance(pluginClazz));
        } catch (Exception e) {
            throw new FormulaRuntimeException("[Swak-Formula] plugin can not instantiation", e);
        }
    }

    private void installPlugin(FormulaPlugin functionPlugin) {
        Class<? extends FormulaPlugin> pluginClazz = functionPlugin.getClass();
        log.info("[Swak-Formula] install function plugin: {}", pluginClazz.getName());
        List<FormulaMetaMethod> formulaMetaMethodList = Lists.newArrayList();
        Arrays.stream(pluginClazz.getDeclaredMethods())
                .forEach(method -> {
                    FormulaMetaMethod formulaMetaMethod = getFormulaMetaMethod(method);
                    String name = formulaMetaMethod.getName();
                    if (PLUGIN_METHOD_MAPPING.get(name) != null) {
                        log.warn("[Swak-Formula] function name {} already exist.", name);
                        return;
                    }
                    PLUGIN_METHOD_MAPPING.put(name, new MethodInvocation(method, functionPlugin));
                    log.debug("[Swak-Formula] mapping {} -> {}.{}", name, pluginClazz.getName(), method.getName());
                    formulaMetaMethodList.add(formulaMetaMethod);
                });
        FUNCTION_CACHE.put(pluginClazz, formulaMetaMethodList);
    }

    private FormulaMetaMethod getFormulaMetaMethod(Method method) {
        FormulaNamed formulaNamed = method.getAnnotation(FormulaNamed.class);
        FormulaMetaMethod formulaMetaMethod = new FormulaMetaMethod()
                .setName(method.getName()).setReturnType(method.getReturnType());
        if (Objects.nonNull(formulaNamed)) {
            formulaMetaMethod.setName(formulaNamed.value()).setDescription(formulaNamed.desc());
        }
        String[] parameterNames = Optional.ofNullable(paramNameDiscoverer.getParameterNames(method))
                .orElse(ArrayUtils.EMPTY_STRING_ARRAY);
        List<FormulaMetaMethod.ParamMetaType> paramMetaTypes = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            FormulaMetaMethod.ParamMetaType paramMetaType = new FormulaMetaMethod.ParamMetaType()
                    .setParamName(i < parameterNames.length ? parameterNames[i] : "")
                    .setParameterType(parameterType);
            paramMetaTypes.add(paramMetaType);
        }
        formulaMetaMethod.setParamMetaTypes(paramMetaTypes);
        FUNCTION_RETURN_TYPE.putIfAbsent(formulaMetaMethod.getName(), formulaMetaMethod);
        return formulaMetaMethod;
    }

    public FormulaMetaMethod getCustomFormula(String functionName) {
        return Optional.ofNullable(FUNCTION_RETURN_TYPE.get(functionName))
                .orElse(FUNCTION_RETURN_TYPE.get(GetterUtil.firstToLowerCase(functionName)));
    }

    public MethodInvocation getMethodInvocation(String functionName) {
        return Optional.ofNullable(PLUGIN_METHOD_MAPPING.get(functionName))
                .orElse(PLUGIN_METHOD_MAPPING.get(GetterUtil.firstToLowerCase(functionName)));
    }


    public List<FormulaMetaMethod> getAllMetaMethods() {
        return Lists.newArrayList(FUNCTION_RETURN_TYPE.values());
    }


    protected static class FormulaPluginRegistryInstance {
        static final FormulaPluginRegistry INSTANCE = new FormulaPluginRegistry();
    }

    public static FormulaPluginRegistry getInstance() {
        return FormulaPluginRegistry.FormulaPluginRegistryInstance.INSTANCE;
    }
}
