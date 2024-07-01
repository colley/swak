package com.swak.core.extension.annotation;

import com.swak.common.dto.base.BizScenario;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 扩展能力
 *
 * @author colley.ma
 * @since  2022/9/19 10:53
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Extension {
    /**
     * 业务编码
     *
     * @return the string
     */
    String bizId() default BizScenario.DEFAULT_BIZ_ID;

    /**
     * Use case string.
     *
     * @return the string
     */
    String useCase() default BizScenario.DEFAULT_USE_CASE;

    /**
     * 场景
     *
     * @return the string
     */
    String scenario() default BizScenario.DEFAULT_SCENARIO;

    /**
     * 描述
     *
     * @return the string
     */
    String desc() default "";
}
