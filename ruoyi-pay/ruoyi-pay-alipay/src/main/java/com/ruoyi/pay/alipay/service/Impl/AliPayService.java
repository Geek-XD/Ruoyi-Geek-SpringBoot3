package com.ruoyi.pay.alipay.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.pay.alipay.service.IAliPayService;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("pay:service:alipay")
@ConditionalOnProperty(prefix = "pay.alipay", name = "enabled", havingValue = "true")
public class AliPayService implements IAliPayService {
    public void callback(Map<String, String> params) {
    }

    @Autowired
    private IPayOrderService payOrderService;

    public String payUrl(PayOrder payOrder) {

        try {
            AlipayTradePagePayResponse response = Factory.Payment.Page().pay(
                    payOrder.getOrderContent(),
                    payOrder.getOrderNumber(),
                    payOrder.getActualAmount(),
                    "");
            return response.getBody();
        } catch (Exception e) {
            throw new ServiceException("创建支付宝支付URL失败");
        }
    }

    @Override
    public String notify(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            String orderNumber = params.get("out_trade_no");
            try {
                if (Factory.Payment.Common().verifyNotify(params)) {
                    payOrderService.updateStatus(orderNumber, "已支付");
                }
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "fail";
    }

    @Override
    public PayOrder query(PayOrder payOrder) {
        try {
            // 使用支付宝SDK查询订单状态
            com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse response = Factory.Payment.Common()
                    .query(payOrder.getOrderNumber());

            // 根据查询结果更新订单状态
            if ("10000".equals(response.code)) {
                String tradeStatus = response.tradeStatus;
                String orderStatus = "";

                // 根据支付宝交易状态映射到系统订单状态
                switch (tradeStatus) {
                    case "TRADE_SUCCESS":
                    case "TRADE_FINISHED":
                        orderStatus = "已支付";
                        break;
                    case "WAIT_BUYER_PAY":
                        orderStatus = "待支付";
                        break;
                    case "TRADE_CLOSED":
                        orderStatus = "已关闭";
                        break;
                    default:
                        orderStatus = "未知状态";
                }

                // 更新订单信息
                payOrderService.updateStatus(payOrder.getOrderNumber(), orderStatus);
            } else {
                throw new ServiceException("查询支付宝订单失败：" + response.subMsg);
            }

            return payOrder;
        } catch (Exception e) {
            throw new ServiceException("查询支付宝订单异常：" + e.getMessage());
        }
    }

    @Override
    public PayOrder refund(PayOrder payOrder) {
        try {
            // 使用支付宝SDK进行退款
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(
                    payOrder.getOrderNumber(),
                    payOrder.getActualAmount());

            // 处理退款结果
            if ("10000".equals(response.code)) {
                payOrderService.updateStatus(payOrder.getOrderNumber(), "已退款");
            } else {
                throw new ServiceException("支付宝退款失败：" + response.subMsg);
            }

            return payOrder;
        } catch (Exception e) {
            throw new ServiceException("支付宝退款异常：" + e.getMessage());
        }
    }
}
