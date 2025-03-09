package com.ruoyi.pay.sqb.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.sqb.service.ISqbPayService;
import com.ruoyi.pay.sqb.service.Impl.SQBServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "sqb支付")
@RestController
@RequestMapping("/pay/sqb")
@ConditionalOnProperty(prefix = "pay.sqb", name = "enabled", havingValue = "true")
public class SQBController extends BaseController {
    @Autowired
    private SQBServiceImpl sqbServiceImpl;
    @Autowired
    private IPayOrderService payOrderServicer;

    @Autowired(required = false)
    private ISqbPayService sqbPayService;

    @Operation(summary = "获取支付url")
    @Parameters(value = {
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @PostMapping("/url/{orderNumber}")
    @Anonymous
    public R<String> url(@PathVariable(name = "orderNumber") String orderNumber) throws Exception {
        PayOrder payOrder = payOrderServicer.selectPayOrderByOrderNumber(orderNumber);
        String url = sqbServiceImpl.payUrl(payOrder);
        return R.ok(url);
    }

    @Operation(summary = "查询支付状态")
    @Parameters(value = {
            @Parameter(name = "orderNumber", description = "订单号", required = true)
    })
    @PostMapping("/query/{orderNumber}")
    public AjaxResult query(@PathVariable(name = "orderNumber") String orderNumber) throws Exception {
        PayOrder payOrder = payOrderServicer.selectPayOrderByOrderNumber(orderNumber);
        return success(sqbServiceImpl.query(payOrder));
    }

    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody PayOrder payOrder) {
        String refund = sqbServiceImpl.refund(payOrder);
        if (refund == null) {
            return error("退款失败");
        }
        Object parse = JSON.parse(refund);
        return success(parse);
    }

    @PostMapping("/notify")
    @Anonymous
    @Operation(summary = "支付回调")
    public AjaxResult notify(@RequestBody JSONObject jsonObject) throws IOException {
        // 验签
        // 修改订单状态
        // 用户自定义行为
        return AjaxResult.success();
    }

}
