package com.swak.autoconfigure.condition;

import com.swak.core.environment.SysEnv;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class EnvironmentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(EnvironmentConditional.class.getName()));
        if (Objects.isNull(attributes)) {
            return false;
        }
        String[] env = attributes.getStringArray("env");
        boolean reversed = attributes.getBoolean("reversed");
        if (ArrayUtils.isEmpty(env)) {
            return true;
        }
        SysEnv sysEnv = null;
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        if (ArrayUtils.isNotEmpty(activeProfiles)) {
            sysEnv = SysEnv.getEnv(activeProfiles[0]);
        }
        if (Objects.isNull(sysEnv)) {
            return true;
        }
        boolean isMatches = false;
        for (String envStr : env) {
            if (sysEnv.eq(envStr)) {
                isMatches = true;
                break;
            }
        }
        if (isMatches && reversed) {
            return false;
        }
        if (isMatches) {
            return true;
        }
        return reversed;
    }
}
