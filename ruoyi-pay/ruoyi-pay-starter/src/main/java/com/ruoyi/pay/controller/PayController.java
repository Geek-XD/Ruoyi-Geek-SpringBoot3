package com.ruoyi.pay.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.service.PayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "支付业务")
@RequestMapping("/pay/{channel}")
@RestController
public class PayController extends BaseController {

    @Autowired(required = false)
    private Map<String, PayService> payServiceMap; // alipay wechat sqb

    @PostConstruct
    public void init() {
        if (payServiceMap == null) {
            payServiceMap = new HashMap<>();
            logger.warn("请注意，没有加载任何支付服务");
        } else {
            payServiceMap.forEach((k, v) -> {
                logger.info("已加载支付服务 {}", k);
            });
        }
    }

    @Autowired
    private IPayOrderService payOrderService;

    @Operation(summary = "支付")
    @Parameters({
            @Parameter(name = "channel", description = "支付方式", required = true),
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @GetMapping("/url/{orderNumber}")
    public AjaxResult url(@PathVariable String channel, @PathVariable String orderNumber) throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        PayOrder payOrder = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(payService.payUrl(payOrder));
    }

    @Anonymous
    @Operation(summary = "支付查询订单")
    @Parameters({
            @Parameter(name = "channel", description = "支付方式", required = true)
    })
    @PostMapping("/notify")
    public String notify(@PathVariable String channel, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        return payService.notify(request, response);
    }

    @Operation(summary = "查询支付状态")
    @Parameters({
            @Parameter(name = "channel", description = "支付方式", required = true),
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @PostMapping("/query/{orderNumber}")
    public AjaxResult query(@PathVariable String channel, @PathVariable(name = "orderNumber") String orderNumber)
            throws Exception {
        PayService payService = payServiceMap.get(channel + "PayService");
        PayOrder payOrder = payOrderService.selectPayOrderByOrderNumber(orderNumber);
        return success(payService.query(payOrder));
    }

    @Operation(summary = "退款")
    @PostMapping("/refund")
    @Parameters({
            @Parameter(name = "channel", description = "支付方式", required = true),
    })
    public AjaxResult refund(@PathVariable String channel, @RequestBody PayOrder payOrder) {
        PayService payService = payServiceMap.get(channel + "PayService");
        return success(payService.refund(payOrder));
    }
}
