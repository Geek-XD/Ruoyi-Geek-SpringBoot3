package com.geek.framework.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.geek.common.core.domain.Message;
import com.geek.common.core.domain.model.LoginUser;
import com.geek.common.utils.JSON;

@Component
public class GeekWebSocketSessionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeekWebSocketSessionRegistry.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> usernameSessions = new ConcurrentHashMap<>();
    private final Map<String, LoginUser> loginUsers = new ConcurrentHashMap<>();
    private final Semaphore socketSemaphore;
    private final GeekWebSocketProperties properties;

    public GeekWebSocketSessionRegistry(GeekWebSocketProperties properties) {
        this.properties = properties;
        this.socketSemaphore = new Semaphore(properties.getMaxOnlineCount());
    }

    public void register(WebSocketSession session, LoginUser loginUser) {
        if (!socketSemaphore.tryAcquire()) {
            throw new IllegalStateException("当前在线人数超过限制数：" + properties.getMaxOnlineCount());
        }
        sessions.put(session.getId(), session);
        if (loginUser != null) {
            loginUsers.put(session.getId(), loginUser);
            usernameSessions.put(loginUser.getUsername(), session);
        }
    }

    public void unregister(String sessionId) {
        WebSocketSession removedSession = sessions.remove(sessionId);
        LoginUser loginUser = loginUsers.remove(sessionId);
        if (loginUser != null) {
            usernameSessions.remove(loginUser.getUsername(), removedSession);
        }
        if (removedSession != null) {
            socketSemaphore.release();
        }
    }

    public WebSocketSession getByUsername(String username) {
        return usernameSessions.get(username);
    }

    public Collection<LoginUser> getUsers() {
        return loginUsers.values();
    }

    public void sendText(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                LOGGER.error("[发送文本消息异常]", e);
            }
        }
    }

    public void sendMessage(WebSocketSession session, Message message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JSON.toJSONString(message)));
            } catch (IOException e) {
                LOGGER.error("[发送消息异常]", e);
            }
        }
    }
}
