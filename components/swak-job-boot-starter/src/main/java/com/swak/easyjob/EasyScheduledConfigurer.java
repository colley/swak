package com.swak.easyjob;

import com.swak.easyjob.annotation.EasyJobInfo;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.util.List;

/**
 * 任务调度
 *
 * @author  yuanchao.ma
 * @since  2022 /8/29 16:33
 */
public interface EasyScheduledConfigurer extends SchedulingConfigurer, BeanFactoryAware, SmartInitializingSingleton, ApplicationContextAware, BeanPostProcessor, BeanClassLoaderAware {

    /**
     * Do registration list.
     *
     * @return the list
     */
    List<EasyJobInfo> doRegistration();
}
