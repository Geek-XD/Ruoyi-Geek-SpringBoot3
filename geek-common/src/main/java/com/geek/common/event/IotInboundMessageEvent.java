package com.geek.common.event;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT入站消息事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInboundMessageEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息来源类型，如 TELEMETRY / ATTRIBUTES / RABBITMQ。
     */
    private String sourceType;

    /**
     * 主题或路由键。
     */
    private String topic;

    /**
     * 外部交换机名称。
     */
    private String exchange;

    /**
     * 外部队列名称。
     */
    private String queue;

    /**
     * 消息时间戳(毫秒)。
     */
    private Long timestamp;

    /**
     * 数据负载。
     */
    private Map<String, Object> payload;

    /**
     * 扩展元数据。
     */
    private Map<String, Object> metadata;
}
