package com.ruoyi.tfa.phone.service.Impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.auth.common.enums.OauthVerificationUse;
import com.ruoyi.auth.common.utils.RandomCodeUtil;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.web.service.UserDetailsServiceImpl;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.tfa.phone.config.DySmsConfig;
import com.ruoyi.tfa.phone.service.DySmsService;
import com.ruoyi.tfa.phone.utils.DySmsUtil;

/**
 * 手机号认证Servcie
 * 
 * @author zlh
 * @date 2024-04-16
 */
@Service("auth:service:dySms")
public class DySmsServiceImpl implements DySmsService {

    @Autowired
    private ISysUserService userService;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SysLoginService sysLoginService;
    @Autowired
    private DySmsConfig dySmsConfig;

    private static final Logger log = LoggerFactory.getLogger(DySmsServiceImpl.class);

    @Override
    public boolean sendCode(String phone, String code, OauthVerificationUse use) {
        if (CacheUtils.hasKey(CacheConstants.PHONE_CODES, use.getValue() + phone)) {
            throw new ServiceException("当前验证码未失效，请在1分钟后再发送");
        }

        try {
            JSONObject templateParams = new JSONObject();
            templateParams.put("code", code);
            DySmsUtil.sendSms(phone, dySmsConfig.getTemplate().get("VerificationCode"), templateParams);
            CacheUtils.put(CacheConstants.PHONE_CODES, use.getValue() + phone, code, 1, TimeUnit.MINUTES);
            log.info("发送手机验证码成功:{ phone: " + phone + " code:" + code + "}");
            return true;
        } catch (Exception e) {
            throw new ServiceException("发送手机验证码异常：" + phone);
        }
    }

    @Override
    public boolean checkCode(String phone, String code, OauthVerificationUse use) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        String cachedCode = CacheUtils.get(CacheConstants.PHONE_CODES, use.getValue() + phone, String.class); // 从缓存中获取验证码
        boolean isValid = code.equals(cachedCode);
        if (isValid) {
            CacheUtils.remove(CacheConstants.PHONE_CODES, use.getValue() + phone);
        }
        return isValid;
    }

    @Override
    public void doLogin(LoginBody loginBody) {
        SysUser sysUser = userService.selectUserByPhone(loginBody.getPhonenumber());
        if (sysUser == null) {
            throw new ServiceException("该手机号未绑定用户");
        } else {
            sendCode(loginBody.getPhonenumber(), RandomCodeUtil.numberCode(6), OauthVerificationUse.LOGIN);
        }
    }

    @Override
    public String doLoginVerify(LoginBody loginBody) {
        if (checkCode(loginBody.getPhonenumber(), loginBody.getCode(), OauthVerificationUse.LOGIN)) {
            SysUser sysUser = userService.selectUserByPhone(loginBody.getPhonenumber());
            if (sysUser == null) {
                throw new ServiceException("该手机号未绑定用户");
            }
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.LOGIN_SUCCESS,
                    MessageUtils.message("user.login.success")));
            LoginUser loginUser = (LoginUser) userDetailsServiceImpl.createLoginUser(sysUser);
            sysLoginService.recordLoginInfo(loginUser.getUserId());
            return tokenService.createToken(loginUser);
        } else {
            throw new ServiceException("验证码错误");
        }
    }

    @Override
    public void doRegister(RegisterBody registerBody) {
        SysUser sysUser = userService.selectUserByPhone(registerBody.getPhonenumber());
        if (sysUser != null) {
            throw new ServiceException("该手机号已绑定用户");
        } else {
            sendCode(registerBody.getPhonenumber(), RandomCodeUtil.numberCode(6), OauthVerificationUse.REGISTER);
        }
    }

    @Override
    public void doRegisterVerify(RegisterBody registerBody) {
        if (checkCode(registerBody.getPhonenumber(), registerBody.getCode(), OauthVerificationUse.REGISTER)) {
            SysUser sysUser = new SysUser();
            sysUser.setUserName(registerBody.getPhonenumber());
            sysUser.setNickName(registerBody.getUsername());
            sysUser.setPassword(SecurityUtils.encryptPassword(registerBody.getPassword()));
            sysUser.setPhonenumber(registerBody.getPhonenumber());
            userService.registerUser(sysUser);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.REGISTER,
                    MessageUtils.message("user.register.success")));
        } else {
            throw new ServiceException("验证码错误");
        }
    }

    public void doReset(RegisterBody registerBody) {
        SysUser sysUser = userService.selectUserByPhone(registerBody.getPhonenumber());
        if (sysUser == null) {
            throw new ServiceException("该手机号未绑定用户");
        } else {
            sendCode(registerBody.getPhonenumber(), RandomCodeUtil.numberCode(6), OauthVerificationUse.RESET);
        }
    }

    public void doResetVerify(RegisterBody registerBody) {
        if (checkCode(registerBody.getPhonenumber(), registerBody.getCode(), OauthVerificationUse.RESET)) {
            SysUser sysUser = userService.selectUserById(SecurityUtils.getUserId());
            sysUser.setPhonenumber(registerBody.getPhonenumber());
            userService.updateUser(sysUser);
        } else {
            throw new ServiceException("验证码错误");
        }
    }

    @Override
    public void doBind(LoginBody loginBody) {
        SysUser sysUser = userService.selectUserByPhone(loginBody.getPhonenumber());
        if (sysUser != null) {
            throw new ServiceException("该手机号已绑定用户");
        }
        sysUser = userService.selectUserById(SecurityUtils.getUserId());
        if (!SecurityUtils.matchesPassword(loginBody.getPassword(), sysUser.getPassword())) {
            throw new ServiceException("密码错误");
        }
        sendCode(loginBody.getPhonenumber(), RandomCodeUtil.numberCode(6), OauthVerificationUse.BIND);
    }

    @Override
    public void doBindVerify(LoginBody loginBody) {
        if (checkCode(loginBody.getPhonenumber(), loginBody.getCode(), OauthVerificationUse.BIND)) {
            SysUser sysUser = userService.selectUserById(SecurityUtils.getUserId());
            if (!SecurityUtils.matchesPassword(loginBody.getPassword(), sysUser.getPassword())) {
                throw new ServiceException("密码错误");
            }
            sysUser.setPhonenumber(loginBody.getPhonenumber());
            userService.updateUser(sysUser);
        } else {
            throw new ServiceException("验证码错误");
        }
    }
}
