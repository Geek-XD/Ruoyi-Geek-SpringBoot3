package com.ruoyi.pay.wx.service;

import com.ruoyi.pay.service.PayService;
import com.wechat.pay.java.service.wexinpayscoreparking.model.Transaction;

public interface IWxPayService extends PayService {
    public void callback(Transaction transaction);
}
