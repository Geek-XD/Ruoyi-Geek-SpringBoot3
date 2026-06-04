package com.geek.framework.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

import com.geek.common.core.domain.Message;

/**
 * WebSocket 消息处理器。
 * <p>
 * 返回 {@code true} 表示消息已被当前处理器消费，后续处理器不再继续执行。
 */
public interface GeekWebSocketMessageHandler {

    boolean handle(WebSocketSession session, Message message);
}
