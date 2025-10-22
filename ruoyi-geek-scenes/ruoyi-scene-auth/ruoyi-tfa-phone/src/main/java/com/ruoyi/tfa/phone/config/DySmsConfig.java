package com.ruoyi.tfa.phone.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ruoyi.tfa.phone.domain.DySmsTemplate;

/**
 * 手机号认证数据
 * 
 * @author Dftre
 * @date 2024-04-16
 */
@Configuration
@ConfigurationProperties("tfa.phone.dysms")
public class DySmsConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private Map<String, DySmsTemplate> template;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public Map<String, DySmsTemplate> getTemplate() {
        return template;
    }

    public void setTemplate(Map<String, DySmsTemplate> template) {
        this.template = template;
    }

}