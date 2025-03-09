package com.ruoyi.pay.service;

import com.ruoyi.pay.domain.PayOrder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PayService {
    String payUrl(PayOrder payOrder);

    String notify(HttpServletRequest servletRequest, HttpServletResponse response);

    String query(PayOrder payOrder);

    String refund(PayOrder payOrder);
}
