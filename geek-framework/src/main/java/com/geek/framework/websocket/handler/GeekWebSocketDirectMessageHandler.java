package com.geek.framework.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.geek.common.core.domain.Message;
import com.geek.common.utils.StringUtils;
import com.geek.framework.websocket.registry.GeekWebSocketSessionRegistry;

@Component
@Order(300)
public class GeekWebSocketDirectMessageHandler implements GeekWebSocketMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeekWebSocketDirectMessageHandler.class);

    private final GeekWebSocketSessionRegistry sessionRegistry;

    public GeekWebSocketDirectMessageHandler(GeekWebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public boolean handle(WebSocketSession session, Message message) {
        if (message == null || StringUtils.isEmpty(message.getReceiver())) {
            return false;
        }
        WebSocketSession receiver = sessionRegistry.getByUsername(message.getReceiver());
        if (receiver == null) {
            LOGGER.warn("无法找到接收者 - {}", message.getReceiver());
            return true;
        }
        sessionRegistry.sendMessage(receiver, message);
        return true;
    }
}
