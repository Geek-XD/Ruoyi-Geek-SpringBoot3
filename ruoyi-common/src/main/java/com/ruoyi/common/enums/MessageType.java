package com.ruoyi.common.enums;

public enum MessageType {

    /**
     * 事件
     */
    EVENT("event"),

    /**
     * 普通消息
     */
    MESSAGE("message"),

    /**
     * 异步消息
     */
    ASYNC_MESSAGE("asyncMessage");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
