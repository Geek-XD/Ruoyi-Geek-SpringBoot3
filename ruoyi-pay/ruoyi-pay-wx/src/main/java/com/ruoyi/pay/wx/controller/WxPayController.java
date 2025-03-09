package com.ruoyi.pay.wx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.wx.service.IWxPayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author zlh
 */
@RestController
@ConditionalOnProperty(prefix = "pay.wechat", name = "enabled", havingValue = "true")
@RequestMapping("/pay/wechat")
public class WxPayController extends BaseController {

    @Autowired
    private IWxPayService wxPayService;

    @Autowired
    private IPayOrderService payOrderService;

    @Operation(summary = "微信支付")
    @Parameters({
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @GetMapping("/url/{orderNumber}")
    public AjaxResult url(@PathVariable(name = "orderNumber") String orderNumber) throws Exception {
        PayOrder payOrder = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(wxPayService.payUrl(payOrder));
    }

    @Anonymous
    @Operation(summary = "微信支付查询订单")
    @PostMapping("/notify")
    public AjaxResult notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        wxPayService.notify(request, response);
        return success();
    }

}