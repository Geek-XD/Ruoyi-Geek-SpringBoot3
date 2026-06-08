package com.geek.framework.websocket.handler;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.geek.common.core.domain.Message;
import com.geek.common.enums.MessageType;
import com.geek.common.utils.StringUtils;
import com.geek.framework.websocket.registry.GeekWebSocketSessionRegistry;

@Component
@Order(200)
public class GeekWebSocketEventMessageHandler implements GeekWebSocketMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeekWebSocketEventMessageHandler.class);

    private final GeekWebSocketSessionRegistry sessionRegistry;

    public GeekWebSocketEventMessageHandler(GeekWebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public boolean handle(WebSocketSession session, Message message) {
        if (message == null || !MessageType.EVENT.equals(message.getType())) {
            return false;
        }
        String subject = message.getSubject();
        if (StringUtils.isEmpty(subject)) {
            LOGGER.warn("事件消息缺少主题: {}", message);
            return true;
        }

        String action = StringUtils.isEmpty(message.getContent())
                ? "broadcast"
                : message.getContent().trim().toLowerCase(Locale.ROOT);
        LOGGER.info("处理 WebSocket 事件 - sessionId={}, action={}, subject={}",
                session.getId(),
                action,
                subject);
        switch (action) {
            case "subscribe" -> {
                sessionRegistry.subscribe(session.getId(), subject);
                sessionRegistry.sendMessage(session, buildResponse(message, subject, "订阅成功"));
            }
            case "unsubscribe" -> {
                sessionRegistry.unsubscribe(session.getId(), subject);
                sessionRegistry.sendMessage(session, buildResponse(message, subject, "取消订阅成功"));
            }
            case "broadcast" -> sessionRegistry.broadcast(subject, message);
            default -> LOGGER.warn("未知的事件操作: {}", message.getContent());
        }
        return true;
    }

    private Message buildResponse(Message request, String subject, String content) {
        return Message.builder()
                .sender("system")
                .messageId(request.getMessageId())
                .type(MessageType.EVENT)
                .subject(subject)
                .content(content)
                .build();
    }
}
