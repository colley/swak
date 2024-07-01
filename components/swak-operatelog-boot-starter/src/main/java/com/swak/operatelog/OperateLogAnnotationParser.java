package com.swak.operatelog;

import com.swak.core.interceptor.SwakAnnotationUtils;
import com.swak.operatelog.annotation.LogAnnotationParser;
import com.swak.operatelog.annotation.OperateLog;
import com.swak.operatelog.annotation.OperateLogOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class OperateLogAnnotationParser implements LogAnnotationParser {

    private static final Set<Class<? extends Annotation>> OPERATION_ANNOTATIONS = new LinkedHashSet<>(1);

    static {
        OPERATION_ANNOTATIONS.add(OperateLog.class);
    }

    @Override
    public Collection<OperateLogOperation> parseAnnotations(Class<?> type) {
        return Collections.emptyList();
    }

    @Override
    public Collection<OperateLogOperation> parseAnnotations(Method method, Class<?> targetClass) {
        Collection<? extends Annotation> anns = SwakAnnotationUtils.computeOperations(method, targetClass,
                OPERATION_ANNOTATIONS);
        if (CollectionUtils.isNotEmpty(anns)) {
            final Collection<OperateLogOperation> operation = new ArrayList<>(1);
            anns.stream().filter(ann -> (ann instanceof OperateLog))
                    .forEach(ann -> operation.add(parseAnnotation(method, targetClass, ann)));
            return operation;
        }
        return Collections.emptyList();
    }

    private OperateLogOperation parseAnnotation(Method method, Class<?> targetClass, Annotation ann) {
        OperateLog operateLog = null;
        if (ann instanceof OperateLog) {
            operateLog = (OperateLog) ann;
        }
        if (operateLog == null) {
            return null;
        }
        OperateLogOperation operateLogOperation = new OperateLogOperation();
        operateLogOperation.setModule(operateLog.module())
                .setContent(operateLog.content())
                .setExcludeField(operateLog.excludeField())
                .setLogArgs(operateLog.logArgs())
                .setOperateType(operateLog.operateType())
                .setLogScope(operateLog.logScope())
                .setLogResult(operateLog.logResult());
        return operateLogOperation;
    }

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, OPERATION_ANNOTATIONS);
    }

}
