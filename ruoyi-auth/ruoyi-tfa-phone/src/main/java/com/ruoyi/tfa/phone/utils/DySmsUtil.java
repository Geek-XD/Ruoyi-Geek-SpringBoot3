package com.ruoyi.tfa.phone.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.tfa.phone.config.DySmsConfig;
import com.ruoyi.tfa.phone.domain.DySmsTemplate;

public class DySmsUtil {
    protected final static Logger logger = LoggerFactory.getLogger(DySmsUtil.class);

    /**
     * 使用AK&SK初始化账号Client
     * 
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    private static Client createClient() throws Exception {
        DySmsConfig dySmsConfig = SpringUtils.getBean(DySmsConfig.class);
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(dySmsConfig.getAccessKeyId())
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(dySmsConfig.getAccessKeySecret());
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 验证参数
     * 
     * @param templateParamJson
     * @param dySmsTemplate
     * @throws Exception
     */
    private static void validateParam(JSONObject templateParamJson, DySmsTemplate dySmsTemplate) {
        String keys = dySmsTemplate.getKeys();
        String[] keyArr = keys.split(",");
        for (String item : keyArr) {
            if (!templateParamJson.containsKey(item)) {
                throw new RuntimeException("模板缺少参数：" + item);
            }
        }
    }

    public static void sendSms(DySmsTemplate dySmsTemplate, JSONObject templateParamJson, String phone)
            throws Exception {
        validateParam(templateParamJson, dySmsTemplate);
        Client client = createClient();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(dySmsTemplate.getSignName())
                .setTemplateCode(dySmsTemplate.getTemplateCode())
                .setTemplateParam(templateParamJson.toJSONString());
        try {
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, new RuntimeOptions());
            if ("OK".equals(sendSmsResponse.getBody().getCode())) {
                logger.info("短信接口返回的数据--- {}", sendSmsResponse.getBody().getMessage());
            } else {
                logger.error("短信接口返回的数据--- {}", sendSmsResponse.getBody().getMessage());
                throw new ServiceException(sendSmsResponse.getBody().getMessage());
            }
        } catch (TeaException error) {
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            Common.assertAsString(error.message);
        }
    }
}