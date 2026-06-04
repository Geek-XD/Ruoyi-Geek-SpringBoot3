package com.geek.framework.websocket.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.geek.common.core.domain.Message;
import com.geek.common.enums.MessageType;
import com.geek.framework.websocket.GeekWebSocketSessionRegistry;

@Component
@Order(100)
public class GeekWebSocketAsyncMessageHandler implements GeekWebSocketMessageHandler {

    private final GeekWebSocketSessionRegistry sessionRegistry;

    public GeekWebSocketAsyncMessageHandler(GeekWebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public boolean handle(WebSocketSession session, Message message) {
        if (message == null || !MessageType.ASYNC_MESSAGE.equals(message.getType())) {
            return false;
        }
        sessionRegistry.sendMessage(session, message);
        return true;
    }
}
