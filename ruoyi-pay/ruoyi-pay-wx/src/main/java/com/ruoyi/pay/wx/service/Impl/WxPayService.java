package com.ruoyi.pay.wx.service.Impl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.wx.config.WxPayConfig;
import com.ruoyi.pay.wx.service.IWxPayService;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.wexinpayscoreparking.model.Transaction;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("wechatPayService")
public class WxPayService implements IWxPayService {

    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private NativePayService nativePayService;

    @Autowired
    private WxPayConfig wxPayAppConfig;

    @Autowired
    private NotificationParser notificationParser;

    public void callback(Transaction transaction) {

    }

    @Override
    public String payUrl(PayOrder payOrder) {
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(Integer.parseInt(payOrder.getActualAmount()));
        request.setAmount(amount);
        request.setAppid(wxPayAppConfig.getAppId());
        request.setMchid(wxPayAppConfig.getWxchantId());
        request.setDescription(payOrder.getOrderContent());
        request.setNotifyUrl(wxPayAppConfig.getNotifyUrl());
        request.setOutTradeNo(payOrder.getOrderNumber());
        PrepayResponse response = nativePayService.prepay(request);
        return response.getCodeUrl();
    }

    @Override
    public String notify(HttpServletRequest request, HttpServletResponse response) {
        String timeStamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String signature = request.getHeader("Wechatpay-Signature");
        String certSn = request.getHeader("Wechatpay-Serial");
        try {
            String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(certSn)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timeStamp)
                    .body(requestBody)
                    .build();
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            String orderNumber = transaction.getOutTradeNo();
            String otherOrderNumber = transaction.getTransactionId();
            String orderState = transaction.getTradeState();
            System.out.println("orderNumber: " + orderNumber);
            System.out.println("otherOrderNumber: " + otherOrderNumber);
            System.out.println("orderState: " + orderState);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
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
