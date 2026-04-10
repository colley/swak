package com.swak.lock.parser;

import java.lang.reflect.Method;
import java.util.Collection;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.exception.DistributedLockException;
import com.swak.common.exception.SwakAssert;
import com.swak.common.exception.ThrowableWrapper;
import com.swak.core.command.SwakMethodProvider;
import com.swak.core.command.fallback.SwakFallbackMethod;
import com.swak.core.expression.SwakExpressionEvaluator;
import com.swak.core.interceptor.SwakAdviceSupport;
import com.swak.core.interceptor.SwakOperationInvoker;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.annotation.LockOperation;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;

/**
 * AOP Alliance MethodInterceptor for declarative Lockable management using the common Lockable infrastructure
 *
 * @author colley.ma
 * @since 3.0.0
 **/
@Slf4j
public class LockableInterceptor extends SwakAdviceSupport {

    private DistributedLock distributedLock;

    private final SwakExpressionEvaluator expressionEvaluator;

    public LockableInterceptor() {
        this.expressionEvaluator = new SwakExpressionEvaluator();
    }

    public LockableInterceptor(DistributedLock distributedLock) {
        this();
        this.distributedLock = distributedLock;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        SwakOperationInvoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                throw ThrowableWrapper.throwableWrapper(ex);
            }
        };
        Object target = invocation.getThis();
        SwakAssert.state(target != null, "Target must not be null");
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
            Class<?> targetClass = getTargetClass(target);
            LockOperationSource lockOperationSource = (LockOperationSource) getOperationSource();
            if (lockOperationSource != null) {
                Collection<LockOperation> operations = lockOperationSource.getBasicOperations(method, targetClass);
                if (!CollectionUtils.isEmpty(operations)) {
                    LockOperation lockOperation = operations.iterator().next();
                    EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method,
                            args, target, target.getClass());
                    String key = expressionEvaluator.key(lockOperation.getKey(), method, evaluationContext).toString();
                    try {
                        if (acquireLock(key, lockOperation)) {
                            return invoker.invoke();
                        }
                        return getFallback(target, method, args, lockOperation);
                    } finally {
                        distributedLock.releaseLock(key);
                    }
                }
            }
        }
        return super.invokeOperation(invoker);
    }

    protected Object getFallback(Object target, Method method, Object[] args, LockOperation lockOperation) {
        if (StringUtils.isEmpty(lockOperation.getFallbackMethod())) {
            throw throwLockException(lockOperation);
        }
        Class<?> targetClass = getTargetClass(target);
        SwakFallbackMethod fallbackMethod = SwakMethodProvider.getInstance().getFallbackMethod(targetClass, method,
                false);
        if (fallbackMethod.getMethod() == null) {
            log.error("[swak-lockable] - No fallback available,fallbackMethod is null,fallbackMethodName:{}",
                    fallbackMethod.getFallbackMethod());
            throw throwLockException(lockOperation);
        }
        fallbackMethod.validateReturnType(method);
        try {
            return fallbackMethod.executeWithArgs(target, args);
        } catch (Throwable e) {
            log.error("[swak-lockable] - getFallback execute error,fallbackMethodName:"
                    + fallbackMethod.getFallbackMethod(), e);
            throw throwLockException(lockOperation);
        }
    }

    private DistributedLockException throwLockException(LockOperation lockOperation) {
        return new DistributedLockException(BasicErrCode.SWAK_OPERA_REPEAT);
    }


    private boolean acquireLock(String key, LockOperation lockOperation) {
        if (lockOperation.getLeaseTime() < 0) {
            return distributedLock.acquireLock(key, lockOperation.getTimeToTry(), lockOperation.getTimeUnit());
        }
        return distributedLock.acquireLock(key, lockOperation.getTimeToTry(), lockOperation.getLeaseTime(),
                lockOperation.getTimeUnit());
    }

    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();
        if (distributedLock == null) {
            // Lazily initialize limitRater Adapter...
            SwakAssert.state(this.beanFactory != null, "distributedLock or BeanFactory must be set on rateLimit aspect");
            try {
                setDistributedLock(this.beanFactory.getBean(DistributedLock.class));
            } catch (NoUniqueBeanDefinitionException ex) {
                throw new IllegalStateException("No distributedLock specified, and no unique bean of type "
                        + "distributedLock found. Mark one as primary or declare a specific distributedLock to use.",
                        ex);
            } catch (NoSuchBeanDefinitionException ex) {
                throw new IllegalStateException(
                        "No distributedLock specified, and no bean of type distributedLock found. "
                                + "Register a distributedLock bean or remove the @Lockable annotation from your configuration.",
                        ex);
            }
        }
        this.initialized = true;
    }

    public void setDistributedLock(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }
}
