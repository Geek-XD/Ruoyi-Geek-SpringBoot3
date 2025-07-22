package com.ruoyi.common.core.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ruoyi.common.enums.MessageType;

public class Message {

    /** 消息唯一标识符 */
    private String messageId;
    /** 发送者标识 */
    private String sender;
    /** 接收者标识 */
    private String receiver;
    /** 消息时间戳 */
    private Instant timestamp;
    /** 消息类型（如命令、聊天、日志、事件等） */
    private MessageType type;
    /** 消息主题或事件名称 */
    private String subject;
    /** 消息数据内容 */
    private String content;
    /** 消息数据负载 */
    private Map<String, Object> payload;
    /** 元数据，用于存储额外的信息 */
    private Map<String, Object> metadata;
    /** 消息状态（如成功、失败、重试等） */
    private String status;
    /** 重试次数 */
    private int retryCount;
    /** 最大重试次数 */
    private int maxRetries;
    /** 重试间隔 */
    private String retryInterval;

    // 构造函数
    public Message() {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public static Message create() {
        return new Message();
    }

    public String getMessageId() {
        return messageId;
    }

    public Message setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public Message setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public String getReceiver() {
        return receiver;
    }

    public Message setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Message setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public Message setType(MessageType type) {
        this.type = type;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Message setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Message setPayload(Map<String, Object> payload) {
        this.payload = payload;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Message setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Message setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Message setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Message setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public String getRetryInterval() {
        return retryInterval;
    }

    public Message setRetryInterval(String retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("messageId", getMessageId())
                .append("sender", getSender())
                .append("receiver", getReceiver())
                .append("timestamp", getTimestamp())
                .append("type", getType())
                .append("subject", getSubject())
                .append("content", getContent())
                .append("payload", getPayload())
                .append("metadata", getMetadata())
                .append("status", getStatus())
                .append("retryCount", getRetryCount())
                .append("maxRetries", getMaxRetries())
                .append("retryInterval", getRetryInterval())
                .toString();
    }
}