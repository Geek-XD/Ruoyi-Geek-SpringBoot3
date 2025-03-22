package com.ruoyi.oauth.wx.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.auth.common.domain.OauthUser;
import com.ruoyi.auth.common.service.IOauthUserService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.web.service.UserDetailsServiceImpl;
import com.ruoyi.oauth.wx.constant.WxPubConstant;
import com.ruoyi.oauth.wx.service.WxLoginService;
import com.ruoyi.system.service.ISysUserService;

@Service
public class WxPubLoginServiceImpl implements WxLoginService {

    @Autowired
    private WxPubConstant wxH5Constant;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IOauthUserService oauthUserService;

    @Override
    public String doLogin(String code) {
        String openid = doAuth(
                wxH5Constant.getUrl(),
                wxH5Constant.getAppId(),
                wxH5Constant.getAppSecret(),
                code).getString("openid");
        OauthUser selectOauthUser = oauthUserService.selectOauthUserByUUID(openid);
        if (selectOauthUser == null) {
            return null;
        }
        SysUser sysUser = userService.selectUserById(selectOauthUser.getUserId());
        if (sysUser == null) {
            throw new ServiceException("该微信未绑定用户");
        }
        LoginUser loginUser = (LoginUser) userDetailsServiceImpl.createLoginUser(sysUser);
        return tokenService.createToken(loginUser);
    }

    @Override
    public String doRegister(OauthUser oauthUser) {
        if (StringUtils.isEmpty(oauthUser.getCode())) {
            return "没有凭证";
        }
        if (oauthUser.getUserId() == null) {
            return "请先注册账号";
        }
        JSONObject doAuth = doAuth(
                wxH5Constant.getUrl(),
                wxH5Constant.getAppId(),
                wxH5Constant.getAppSecret(),
                oauthUser.getCode());
        oauthUser.setOpenId(doAuth.getString("openid"));
        oauthUser.setUuid(doAuth.getString("openid"));
        oauthUser.setSource("WXPub");
        oauthUser.setAccessToken(doAuth.getString("sessionKey"));
        oauthUserService.insertOauthUser(oauthUser);
        return "";
    }

}
