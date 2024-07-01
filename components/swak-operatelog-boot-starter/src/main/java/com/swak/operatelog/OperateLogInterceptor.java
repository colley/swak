package com.swak.operatelog;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.exception.SwakAssert;
import com.swak.common.exception.ThrowableWrapper;
import com.swak.common.exception.core.BaseException;
import com.swak.common.util.StringPool;
import com.swak.core.expression.SwakExpressionEvaluator;
import com.swak.core.interceptor.SwakAdviceSupport;
import com.swak.core.interceptor.SwakOperationInvoker;
import com.swak.core.web.ServletUtils;
import com.swak.operatelog.annotation.LogScopeEnum;
import com.swak.operatelog.annotation.OperateDataLog;
import com.swak.operatelog.annotation.OperateLogOperation;
import com.swak.operatelog.common.PropertyPreExcludeFilter;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Slf4j
public class OperateLogInterceptor extends SwakAdviceSupport {

    private static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};
    private SwakExpressionEvaluator expressionEvaluator;
    private LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer;
    private OperateLogService operateLogService;

    private OperateLogProducer operateLogProducer;


    public OperateLogInterceptor() {
        this.expressionEvaluator = new SwakExpressionEvaluator();
        this.localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    public OperateLogInterceptor(OperateLogService operateLogService) {
        this();
        this.operateLogService = operateLogService;
        this.operateLogProducer = new OperateLogProducer(operateLogService);
        this.operateLogProducer.startWork();
    }

    private static void fillRequestFields(OperateDataLog operateDataLog) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        operateDataLog.setRequestMethod(request.getMethod())
                .setRequestUrl(request.getRequestURI())
                .setUserIp(ServletUtils.getIpAddr(request))
                .setUserAgent(ServletUtils.getUserAgent(request));
    }

    private static RequestMethod obtainFirstLogRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtils.isEmpty(requestMethods)) {
            return null;
        }
        return Arrays.stream(requestMethods).filter(requestMethod ->
                requestMethod == RequestMethod.POST
                        || requestMethod == RequestMethod.PUT
                        || requestMethod == RequestMethod.DELETE)
                .findFirst().orElse(null);
    }

    private static RequestMethod obtainFirstMatchRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtils.isEmpty(requestMethods)) {
            return null;
        }
        // 优先，匹配最优的 POST、PUT、DELETE
        RequestMethod result = obtainFirstLogRequestMethod(requestMethods);
        if (result != null) {
            return result;
        }
        // 然后，匹配次优的 GET
        result = Arrays.stream(requestMethods).filter(requestMethod -> requestMethod == RequestMethod.GET)
                .findFirst().orElse(null);
        if (result != null) {
            return result;
        }
        // 兜底，获得第一个
        return requestMethods[0];
    }

    private static String convertOperateLogType(RequestMethod requestMethod) {
        if (requestMethod == null) {
            return null;
        }
        return requestMethod.name();
    }

    private static boolean isIgnoreArgs(Object object) {
        Class<?> clazz = object.getClass();
        // 处理数组的情况
        if (clazz.isArray()) {
            return IntStream.range(0, Array.getLength(object))
                    .anyMatch(index -> isIgnoreArgs(Array.get(object, index)));
        }
        // 递归，处理数组、Collection、Map 的情况
        if (Collection.class.isAssignableFrom(clazz)) {
            return ((Collection<?>) object).stream()
                    .anyMatch((Predicate<Object>) OperateLogInterceptor::isIgnoreArgs);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return isIgnoreArgs(((Map<?, ?>) object).values());
        }
        // obj
        return object instanceof MultipartFile
                || object instanceof HttpServletRequest
                || object instanceof HttpServletResponse
                || object instanceof BindingResult;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        SwakOperationInvoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                throw ThrowableWrapper.throwableWrapper(ex);
            }
        };
        Object target = invocation.getThis();
        SwakAssert.state(target != null, "Target must not be null");
        Method method = AopUtils.getMostSpecificMethod(invocation.getMethod(), getTargetClass(target));
        try {
            return execute(aopAllianceInvoker, target, method, invocation.getArguments());
        } catch (ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }

    @Nullable
    protected Object execute(SwakOperationInvoker invoker, Object target, Method method, Object[] args) {
        // Check whether aspect is enabled (to cope with cases where the AJ is pulled in automatically)
        if (this.initialized) {
            //未开启操作日志
            if (Objects.isNull(operateLogService) || !operateLogService.isOperateLogEnabled()) {
                return super.invokeOperation(invoker);
            }
            //开启业务操作日志记录
            Class<?> targetClass = getTargetClass(target);
            OperateLogOperationSource operationSource = (OperateLogOperationSource) getOperationSource();
            if (operationSource != null) {
                Collection<OperateLogOperation> operations = operationSource.getBasicOperations(method, targetClass);
                if (!CollectionUtils.isEmpty(operations)) {
                    OperateLogOperation operation = operations.iterator().next();
                    String content = this.getContent(operation, target, method, args);
                    return this.handler(invoker, method, args, content, operation);
                }
            }
        }
        return super.invokeOperation(invoker);
    }

    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();
        this.initialized = true;
    }

    private String getContent(OperateLogOperation operation, Object target, Method method, Object[] args) {
        String content = Optional.ofNullable(operation.getContent()).orElse(StringPool.EMPTY);
        if (StringUtils.isEmpty(content) || !content.contains("#")) {
            return content;
        }
        try {
            EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method,
                    args, target, target.getClass());
            content = expressionEvaluator.key(content, method, evaluationContext).toString();
        } catch (Exception e) {
            log.error("operate content error,message:" + e.getMessage(), e);
        }
        return content;
    }

    private Object handler(SwakOperationInvoker invoker, Method method, Object[] args, String content, OperateLogOperation operation) {
        // 目前，只有管理员，才记录操作日志！所以非管理员，直接调用，不进行记录
        // 记录开始时间
        OperateDataLog operateDataLog = new OperateDataLog();
        Long startTime = System.currentTimeMillis();
        try {
            // 执行原有方法
            Object result = invoker.invoke();
            //只记录成功或者失败的
            if (result instanceof Response) {
                Response<?> commonResult = (Response<?>) result;
                boolean isSuccess = commonResult.isSuccess();
                if (isSuccess && LogScopeEnum.isFail(operation.getLogScope())) {
                    return result;
                }
                if (!isSuccess && LogScopeEnum.isSuccess(operation.getLogScope())) {
                    return result;
                }
            }
            // 记录正常执行时的操作日志
            operateDataLog.setContent(content);
            this.logHandler(operation, method, args, startTime, operateDataLog, result, null);
            return result;
        } catch (Throwable exception) {
            if (!LogScopeEnum.isSuccess(operation.getLogScope())) {
                this.logHandler(operation, method, args, startTime, operateDataLog, null, exception);
            }
            throw exception;
        }
    }

    private void logHandler(OperateLogOperation operation, Method method, Object[] args, Long startTime, OperateDataLog operateDataLog, Object result, Throwable exception) {
        try {
            // 真正记录操作日志
            operateDataLog.setTraceId(MDC.get(OperateDataLog.TRACE_ID))
                    .setStartTime(new Date(startTime));
            // 补全模块信息
            fillModuleFields(operateDataLog, method, operation);
            // 补全请求信息
            fillRequestFields(operateDataLog);
            // 补全方法信息
            fillMethodFields(operateDataLog, operation, method, args, startTime, result, exception);
            // 异步记录日志
            operateLogProducer.sendLogMessage(operateDataLog);
        } catch (Throwable ex) {
            try {
                log.error("[log][记录操作日志时，发生异常，其中参数是 operateLog({}) apiOperation({}) result({}) exception({}) ]",
                        JSON.toJSONString(operation), result, exception, ex);
            } catch (Exception e) {
                //ignore
            }
        }
    }

    private void fillModuleFields(OperateDataLog operateDataLog, Method method, OperateLogOperation operation) {
        // module 属性
        operateDataLog.setModule(operation.getModule()).setOperateType(operation.getOperateType());
        if (StringUtils.isEmpty(operateDataLog.getOperateType())) {
            RequestMethod requestMethod = obtainFirstMatchRequestMethod(obtainRequestMethod(method));
            operateDataLog.setOperateType(convertOperateLogType(requestMethod));
        }
    }

    private void fillMethodFields(OperateDataLog operateDataLog, OperateLogOperation operation, Method method, Object[] args,
                                  Long startTime, Object result, Throwable exception) {
        operateDataLog.setJavaMethod(getMethodName(method));
        Set<String> excludeFieldSet = Sets.newHashSet(EXCLUDE_PROPERTIES);
        if (ArrayUtils.isNotEmpty(operation.getExcludeField())) {
            excludeFieldSet.addAll(Arrays.asList(operation.getExcludeField()));
        }
        PropertyPreExcludeFilter propertyPreExcludeFilter = new PropertyPreExcludeFilter(excludeFieldSet.toArray(new String[]{}));
        if (operation.isLogArgs()) {
            operateDataLog.setJavaMethodArgs(obtainMethodArgs(method, args, propertyPreExcludeFilter));
        }
        if (operation.isLogResult()) {
            operateDataLog.setResultData(obtainResultData(result, propertyPreExcludeFilter));
        }
        operateDataLog.setCostTime(System.currentTimeMillis() - startTime);
        // （正常）处理 resultCode 和 resultMsg 字段
        if (result instanceof Response) {
            Response<?> commonResultImpl = (Response<?>) result;
            operateDataLog.setResultCode(commonResultImpl.getCode());
            operateDataLog.setResultMsg(commonResultImpl.getMsg());
        } else {
            operateDataLog.setResultCode(BasicErrCode.SUCCESS.getCode());
        }
        // （异常）处理 resultCode 和 resultMsg 字段
        if (exception != null) {
            if (exception instanceof BaseException) {
                BaseException baseException = (BaseException) exception;
                operateDataLog.setResultCode(baseException.getErrCode());
            } else {
                operateDataLog.setResultCode(BasicErrCode.BIZ_ERROR.getCode());
            }
            operateDataLog.setResultMsg(ExceptionUtils.getRootCauseMessage(exception));
        }
    }

    private RequestMethod[] obtainRequestMethod(Method method) {
        RequestMapping requestMapping = AnnotationUtils.getAnnotation( // 使用 Spring 的工具类，可以处理 @RequestMapping 别名注解
                method, RequestMapping.class);
        return requestMapping != null ? requestMapping.method() : new RequestMethod[]{};
    }

    private String obtainMethodArgs(Method method, Object[] argValues, PropertyPreExcludeFilter propertyPreExcludeFilter) {
        String[] argNames = localVariableTableParameterNameDiscoverer.getParameterNames(method);
        if (argNames == null || ArrayUtils.isEmpty(argNames)) {
            return StringPool.EMPTY;
        }
        // 拼接参数,保留参数的顺序
        Map<String, Object> argsMap = Maps.newLinkedHashMapWithExpectedSize(argValues.length);
        for (int i = 0; i < argNames.length; i++) {
            String argName = argNames[i];
            Object argValue = argValues[i];
            // 被忽略时，标记为 ignore 字符串，避免和 null 混在一起
            argsMap.put(argName, !isIgnoreArgs(argValue) ? argValue : "[ignore]");
        }
        return JSON.toJSONString(argsMap, propertyPreExcludeFilter);
    }

    private String obtainResultData(Object result, PropertyPreExcludeFilter propertyPreExcludeFilter) {
        Object lastResult = result;
        if (result instanceof Response) {
            lastResult = ((Response<?>) result).getData();
        }
        if (lastResult instanceof String) {
            return (String) lastResult;
        }
        //脱敏
        return JSON.toJSONString(lastResult, propertyPreExcludeFilter);
    }

    private String getMethodName(Method method){
        List<String> methodNames = Lists.newArrayList();
        methodNames.add(method.getDeclaringClass().getName()+"."+method.getName());
        methodNames.add("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        if(ArrayUtils.isNotEmpty(parameterTypes)){
            List<String> parameterName =
                    Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.toList());
            methodNames.add(StringUtils.joinWith(",",parameterName));
        }
        methodNames.add(")");
        return StringUtils.joinWith("",methodNames);
    }
}
