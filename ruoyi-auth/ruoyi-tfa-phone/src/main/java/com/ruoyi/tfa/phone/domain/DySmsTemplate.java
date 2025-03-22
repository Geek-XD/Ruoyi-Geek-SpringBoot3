package com.ruoyi.tfa.phone.domain;

/**
 * 手机号认证短信模板
 * 
 * @author Dftre
 * @date 2024-04-16
 */
public class DySmsTemplate {
    /**
     * 短信模板编码
     */
    private String templateCode;
    /**
     * 签名
     */
    private String signName;
    /**
     * 短信模板必需的数据名称，多个key以逗号分隔，此处配置作为校验
     */
    private String keys;

    public DySmsTemplate(String templateCode, String signName, String keys) {
        this.templateCode = templateCode;
        this.signName = signName;
        this.keys = keys;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }
}
