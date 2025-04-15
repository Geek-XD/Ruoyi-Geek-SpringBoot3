package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 设置Anonymous注解允许匿名访问的url
 * 
 * @author ruoyi
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private ApplicationContext applicationContext;

    private List<String> urls = new ArrayList<>();

    public String ASTERISK = "*";

    static class Pair {
        public RequestMappingInfo first;
        public Anonymous second;

        public Pair(RequestMappingInfo first, Anonymous second) {
            this.first = first;
            this.second = second;
        }
    }

    @Override
    public void afterPropertiesSet() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping",RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        String matching = SpringUtils.getRequiredProperty("spring.mvc.pathmatch.matching-strategy").toLowerCase();
        map.keySet()
                .stream()
                .flatMap(info -> {
                    HandlerMethod handlerMethod = map.get(info);
                    // 获取方法上边的注解 替代path variable 为 *
                    Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
                    // 获取类上边的注解, 替代path variable 为 *
                    Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
                    return Arrays.stream(new Pair[] { new Pair(info, method), new Pair(info, controller) });
                })
                .filter(pair -> pair.second != null)
                .flatMap(pair -> switch (matching) {
                    case "ant_path_matcher" ->
                        Objects.requireNonNull(pair.first.getPatternsCondition().getPatterns()).stream();
                    case "path_pattern_parser" ->
                        Objects.requireNonNull(pair.first.getPathPatternsCondition().getPatternValues())
                                .stream();
                    default -> Objects.requireNonNull(pair.first.getPatternsCondition().getPatterns()).stream();
                })
                .map(url -> RegExUtils.replaceAll(url, PATTERN, ASTERISK))
                .forEach(urls::add);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
