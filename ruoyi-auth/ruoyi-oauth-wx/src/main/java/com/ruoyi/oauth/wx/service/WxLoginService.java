package com.ruoyi.oauth.wx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.auth.common.domain.OauthUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.http.HttpClientUtil;

public interface WxLoginService {

    public String doLogin(String code);

    public String doRegister(OauthUser oauthUser);

    public default JSONObject doAuth(String url, String appid, String secret, String code) {
        StringBuilder builder = new StringBuilder(url);
        builder.append("?appid=").append(appid)
                .append("&secret=").append(secret)
                .append("&js_code=").append(code)
                .append("&grant_type=").append("authorization_code");
        String getMessageUrl = builder.toString();
        String result = HttpClientUtil.sendHttpGet(getMessageUrl);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject.containsKey("openid")) {
            String openid = jsonObject.getString("openid");
            String sessionKey = jsonObject.getString("session_key");
            System.out.println("openid:" + openid);
            System.out.println("sessionKey:" + sessionKey);
            return jsonObject;
        } else {
            throw new ServiceException(jsonObject.getString("errmsg"), jsonObject.getIntValue("errcode"));
        }
    }
}
