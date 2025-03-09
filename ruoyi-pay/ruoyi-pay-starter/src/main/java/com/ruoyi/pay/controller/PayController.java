package com.ruoyi.pay.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.service.PayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/pay/<channel>")
public class PayController extends BaseController {
    @Autowired
    private Map<String, PayService> payServiceMap; // alipay wechat sqb

    @Autowired
    private IPayOrderService payOrderService;

    @Operation(summary = "微信支付")
    @Parameters({
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @GetMapping("/url/{orderNumber}")
    public AjaxResult url(@PathVariable String channel, @PathVariable String orderNumber) throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        PayOrder payOrder = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(payService.payUrl(payOrder));
    }

    @Anonymous
    @Operation(summary = "微信支付查询订单")
    @PostMapping("/notify")
    public String notify(@PathVariable String channel, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        return payService.notify(request, response);
    }

    @Operation(summary = "查询支付状态")
    @Parameters(value = {
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @PostMapping("/query/{orderNumber}")
    public AjaxResult query(@PathVariable String channel, @PathVariable(name = "orderNumber") String orderNumber)
            throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        PayOrder payOrder = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(payService.query(payOrder));
    }

    @PostMapping("/refund")
    public AjaxResult refund(@PathVariable String channel, @RequestBody PayOrder payOrder) {
        PayService payService = payServiceMap.get(channel + "PayService");
        String refund = payService.refund(payOrder);
        if (refund == null) {
            return error("退款失败");
        }
        Object parse = JSON.parse(refund);
        return success(parse);
    }
}
