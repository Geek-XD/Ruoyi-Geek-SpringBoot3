package com.ruoyi.pay.wx.service.Impl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.wx.config.WxPayConfig;
import com.ruoyi.pay.wx.service.IWxPayService;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.Transaction.TradeStateEnum;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByIdRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("wechatPayService")
@ConditionalOnProperty(prefix = "pay.wechat", name = "enabled", havingValue = "true")
public class WxPayService implements IWxPayService {

    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private NativePayService nativePayService;

    @Autowired
    private WxPayConfig wxPayAppConfig;

    @Autowired
    private NotificationParser notificationParser;

    @Autowired
    private RefundService refundService;

    @Override
    public String payUrl(PayOrder payOrder) {
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(Integer.parseInt(payOrder.getActualAmount()));
        request.setAmount(amount);
        request.setAppid(wxPayAppConfig.getAppId());
        request.setMchid(wxPayAppConfig.getMerchantId());
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
            TradeStateEnum orderState = transaction.getTradeState();
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
    public PayOrder query(PayOrder payOrder) {
        QueryOrderByIdRequest queryRequest = new QueryOrderByIdRequest();
        queryRequest.setMchid(wxPayAppConfig.getMerchantId());
        queryRequest.setTransactionId(payOrder.getOrderNumber());
        try {
            Transaction result = nativePayService.queryOrderById(queryRequest);
            System.out.println(result.getTradeState());
        } catch (ServiceException e) {
            System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
            System.out.printf("reponse body=[%s]\n", e.getResponseBody());
        }
        return payOrder;
    }

    @Override
    public PayOrder refund(PayOrder payOrder) {
        CreateRequest request = new CreateRequest();
        request.setTransactionId(payOrder.getOrderNumber());
        request.setOutRefundNo(payOrder.getOrderNumber());
        request.setOutTradeNo(payOrder.getOrderNumber());
        Refund refund = refundService.create(request);
        Status status = refund.getStatus();
        if (status.equals(Status.SUCCESS)) {
            payOrderService.updateStatus(payOrder.getOrderNumber(), "已退款");
        }
        return payOrder;
    }

}
