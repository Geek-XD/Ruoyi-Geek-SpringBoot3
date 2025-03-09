package com.ruoyi.pay.alipay.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.pay.alipay.service.IAliPayService;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("alipayPayService")
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
    public String query(PayOrder payOrder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'query'");
    }

    @Override
    public String refund(PayOrder payOrder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refund'");
    }
}
