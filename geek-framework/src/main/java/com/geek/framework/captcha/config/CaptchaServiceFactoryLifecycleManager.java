package com.geek.framework.captcha.config;

import java.util.ServiceLoader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;

@Component
public class CaptchaServiceFactoryLifecycleManager implements BeanFactoryPostProcessor, PriorityOrdered {

    static void refreshFactoryCaches(ClassLoader classLoader) {
        clearFactoryCaches();
        for (CaptchaCacheService cacheService : ServiceLoader.load(CaptchaCacheService.class, classLoader)) {
            CaptchaServiceFactory.cacheService.put(cacheService.type(), cacheService);
        }
        for (CaptchaService captchaService : ServiceLoader.load(CaptchaService.class, classLoader)) {
            CaptchaServiceFactory.instances.put(captchaService.captchaType(), captchaService);
        }
    }

    static void clearFactoryCaches() {
        CaptchaServiceFactory.cacheService.clear();
        CaptchaServiceFactory.instances.clear();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        refreshFactoryCaches(resolveClassLoader(beanFactory));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private ClassLoader resolveClassLoader(ConfigurableListableBeanFactory beanFactory) {
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = CaptchaServiceFactoryLifecycleManager.class.getClassLoader();
        }
        return classLoader;
    }
}
