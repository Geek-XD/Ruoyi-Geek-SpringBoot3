package com.geek.framework.captcha.config;

import java.util.ServiceLoader;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;

public class CaptchaServiceFactoryLifecycleManager {

    public static void refreshFactoryCaches() {
        clearFactoryCaches();
        ClassLoader classLoader = resolveClassLoader();
        for (CaptchaCacheService cacheService : ServiceLoader.load(CaptchaCacheService.class, classLoader)) {
            CaptchaServiceFactory.cacheService.put(cacheService.type(), cacheService);
        }
        for (CaptchaService captchaService : ServiceLoader.load(CaptchaService.class, classLoader)) {
            CaptchaServiceFactory.instances.put(captchaService.captchaType(), captchaService);
        }
    }

    public static void clearFactoryCaches() {
        CaptchaServiceFactory.cacheService.clear();
        CaptchaServiceFactory.instances.clear();
    }

    private static ClassLoader resolveClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = CaptchaServiceFactoryLifecycleManager.class.getClassLoader();
        }
        return classLoader;
    }
}
