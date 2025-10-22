package com.ruoyi.middleware.rabbitmq.controller;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.Message;

/**
 * @Author : JCccc
 * @CreateTime : 2019/9/3
 * @Description :
 **/
@RestController
@RequestMapping("/rabbitmq")
public class SendMessageController {

    @Autowired
    RabbitTemplate rabbitTemplate; // 使用RabbitTemplate,这提供了接收/发送等等方法

    @Anonymous
    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage() {
        // 将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("TestDirectExchange", "TestDirectRouting",
                Message.create()
                        .setPayload(Map.of("message", "你好"))
                        .setReceiver("接收者")
                        .setSender("发送者"));
        return "ok";
    }

}