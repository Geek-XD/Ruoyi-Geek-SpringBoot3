package com.ruoyi.pay.alipay.controller;

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
import com.ruoyi.pay.alipay.service.IAliPayService;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author zlh
 */
@RestController
@RequestMapping("/pay/alipay")
@ConditionalOnProperty(prefix = "pay.alipay", name = "enabled", havingValue = "true")
@Tag(name = "【支付宝】管理")
public class AliPayController extends BaseController {

    @Autowired
    private IAliPayService aliPayService;

    @Autowired
    private IPayOrderService payOrderService;

    @Anonymous
    @Operation(summary = "支付宝支付")
    @Parameters({
            @Parameter(name = "orderId", description = "订单号", required = true)
    })
    @GetMapping("/url/{orderNumber}")
    public AjaxResult pay(@PathVariable(name = "orderNumber") String orderNumber) {
        PayOrder aliPay = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(aliPayService.payUrl(aliPay));
    }

    @Anonymous
    @Operation(summary = "支付宝支付回调")
    @PostMapping("/notify")
    public AjaxResult notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        aliPayService.notify(request, response);
        return success("success");
    }
}
