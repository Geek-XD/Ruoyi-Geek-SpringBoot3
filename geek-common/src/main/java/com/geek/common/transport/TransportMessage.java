package com.geek.common.transport;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中性传输消息信封，用于承载外部通道进入应用的消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息来源，如 rabbitmq / netty / websocket。
     */
    private String source;

    /**
     * 传输通道，如 mq / tcp / ws。
     */
    private String channel;

    /**
     * 主题、路由键或协议动作。
     */
    private String topic;

    /**
     * 业务负载。
     */
    private Map<String, Object> payload;

    /**
     * 传输元数据。
     */
    private Map<String, Object> metadata;

    /**
     * 事件时间戳（毫秒）。
     */
    private Long timestamp;
}
