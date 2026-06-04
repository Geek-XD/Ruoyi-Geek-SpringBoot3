package com.geek.framework.websocket;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.geek.common.core.domain.Message;
import com.geek.common.core.domain.Message.MessageBuilder;
import com.geek.common.core.domain.model.LoginUser;
import com.geek.common.utils.JSON;
import com.geek.common.utils.StringUtils;
import com.geek.framework.websocket.handler.GeekWebSocketMessageHandler;

@Component
public class GeekWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeekWebSocketHandler.class);

    private final GeekWebSocketSessionRegistry sessionRegistry;
    private final List<GeekWebSocketMessageHandler> messageHandlers;

    public GeekWebSocketHandler(GeekWebSocketSessionRegistry sessionRegistry,
            List<GeekWebSocketMessageHandler> messageHandlers) {
        this.sessionRegistry = sessionRegistry;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        MessageBuilder message = Message.builder().sender("system");
        LoginUser loginUser = (LoginUser) session.getAttributes().get(GeekWebSocketHandshakeInterceptor.LOGIN_USER_ATTR);
        if (StringUtils.isNull(loginUser)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        try {
            sessionRegistry.register(session, loginUser);
            LOGGER.info("建立连接 - sessionId={}, username={}, uri={}, remote={}",
                    session.getId(),
                    loginUser.getUsername(),
                    session.getUri(),
                    session.getRemoteAddress());
            LOGGER.info("当前人数 - {}", sessionRegistry.getUsers().size());
            message.content("连接成功,你好" + loginUser.getUsername());
            sessionRegistry.sendMessage(session, message.build());
        } catch (IllegalStateException e) {
            LOGGER.warn("WebSocket 连接失败 - {}", e.getMessage());
            message.content(e.getMessage());
            sessionRegistry.sendMessage(session, message.build());
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOGGER.info("关闭连接 - {}, 状态: {}", session.getId(), status);
        sessionRegistry.unregister(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        LOGGER.info("连接异常 - {}", session.getId());
        LOGGER.info("异常信息 - {}", exception.getMessage());
        sessionRegistry.unregister(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Message payload = JSON.parseObject(message.getPayload(), Message.class);
        if (payload == null) {
            LOGGER.warn("忽略无效的 WebSocket 消息: {}", message.getPayload());
            return;
        }
        LOGGER.info("收到 WebSocket 消息 - sessionId={}, type={}, subject={}, content={}",
                session.getId(),
                payload.getType(),
                payload.getSubject(),
                payload.getContent());
        for (GeekWebSocketMessageHandler messageHandler : messageHandlers) {
            if (messageHandler.handle(session, payload)) {
                return;
            }
        }
        LOGGER.warn("未匹配到 WebSocket 消息处理器: {}", message.getPayload());
    }
}
