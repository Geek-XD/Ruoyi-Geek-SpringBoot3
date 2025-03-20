package com.ruoyi.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.auth.common.service.TfaService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.RegisterBody;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/auth/{channel}") // dySms mail
public class TfaController extends BaseController {

    @Autowired(required = false)
    Map<String, TfaService> tfaServiceMap;

    @PostConstruct
    public void init() {
        if (tfaServiceMap == null) {
            tfaServiceMap = new HashMap<>();
            logger.warn("请注意，没有加载任何双认证服务");
        } else {
            tfaServiceMap.forEach((k, v) -> {
                logger.info("已加载双认证服务 {}", k);
            });
        }
    }

    @PostMapping("/send/bind")
    public AjaxResult send(@PathVariable String channel, @RequestBody LoginBody loginBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        tfaService.doBind(loginBody);
        return success();
    }

    @PostMapping("/verify/bind") // 发送验证码
    public AjaxResult verify(@PathVariable String channel, @RequestBody LoginBody loginBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        tfaService.doBindVerify(loginBody);
        return success();
    }

    @PostMapping("/send/register")
    @Anonymous
    public AjaxResult sendRegister(@PathVariable String channel, @RequestBody RegisterBody registerBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        tfaService.doRegister(registerBody);
        return success();
    }

    @PostMapping("/verify/register")
    @Anonymous
    public AjaxResult verifyRegister(@PathVariable String channel, @RequestBody RegisterBody registerBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        tfaService.doRegisterVerify(registerBody);
        return success();
    }

    @PostMapping("/send/login")
    @Anonymous
    public AjaxResult sendLogin(@PathVariable String channel, @RequestBody LoginBody loginBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        tfaService.doLogin(loginBody);
        return success();
    }

    @PostMapping("/verify/login")
    @Anonymous
    public AjaxResult verifyLogin(@PathVariable String channel, @RequestBody LoginBody loginBody) {
        TfaService tfaService = tfaServiceMap.get(channel + "AuthService");
        return success(tfaService.doLoginVerify(loginBody));
    }
}
