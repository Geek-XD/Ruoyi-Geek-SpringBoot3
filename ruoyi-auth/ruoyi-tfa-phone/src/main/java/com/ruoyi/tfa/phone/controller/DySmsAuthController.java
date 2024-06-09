package com.ruoyi.tfa.phone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.tfa.phone.service.Impl.DySmsServiceImpl;

/**
 * 手机号认证Controller
 * 
 * @author zlh
 * @date 2024-04-16
 */
@RestController
@RequestMapping("/tfa/phone")
public class DySmsAuthController extends BaseController {
    @Autowired
    public DySmsServiceImpl dySmsService;

    @PostMapping("/send/bind") // 发送验证码
    public AjaxResult send(@RequestBody LoginBody loginBody) {
        dySmsService.doBind(loginBody);
        return success();
    }

    @PostMapping("/verify/bind") // 发送验证码
    public AjaxResult verify(@RequestBody LoginBody loginBody) {
        dySmsService.doBindVerify(loginBody);
        return success();
    }
}
