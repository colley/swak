package com.swak.autoconfigure.runner;

import com.swak.common.validation.ValidatorUtils;
import com.swak.core.aware.InitializedAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Objects;

/**
 * @author colley
 */
@Slf4j
@Component
public class ValidatorInitializedAware implements InitializedAware {

    @Resource
    private Validator validator;

    @Override
    public void afterInstantiated() {
        if (Objects.isNull(validator)) {
            log.warn("Validator is null please set validator in Spring Bean");
            return;
        }
        ValidatorUtils.setValidator(validator);
    }
}
