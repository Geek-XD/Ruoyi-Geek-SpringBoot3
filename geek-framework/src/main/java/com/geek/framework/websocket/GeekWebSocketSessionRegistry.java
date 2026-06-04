package com.geek.framework.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
    private final Map<String, Set<String>> subjectSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
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
        unsubscribeAll(sessionId);
        if (removedSession != null) {
            socketSemaphore.release();
        }
    }

    /**
     * 订阅主题
     * 
     * @param sessionId 会话ID
     * @param subject 主题名称
     */
    public void subscribe(String sessionId, String subject) {
        if (sessionId == null || subject == null) {
            return;
        }
        subjectSubscriptions.computeIfAbsent(subject, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(subject);
        LOGGER.info("会话 {} 订阅主题: {}", sessionId, subject);
    }

    /**
     * 取消订阅主题
     * 
     * @param sessionId 会话ID
     * @param subject 主题名称
     */
    public void unsubscribe(String sessionId, String subject) {
        if (sessionId == null || subject == null) {
            return;
        }
        Set<String> subscribers = subjectSubscriptions.get(subject);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                subjectSubscriptions.remove(subject);
            }
        }
        Set<String> subjects = sessionSubscriptions.get(sessionId);
        if (subjects != null) {
            subjects.remove(subject);
            if (subjects.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }
        LOGGER.info("会话 {} 取消订阅主题: {}", sessionId, subject);
    }

    /**
     * 取消所有订阅
     * 
     * @param sessionId 会话ID
     */
    public void unsubscribeAll(String sessionId) {
        Set<String> subjects = sessionSubscriptions.remove(sessionId);
        if (subjects != null) {
            for (String subject : subjects) {
                Set<String> subscribers = subjectSubscriptions.get(subject);
                if (subscribers != null) {
                    subscribers.remove(sessionId);
                    if (subscribers.isEmpty()) {
                        subjectSubscriptions.remove(subject);
                    }
                }
            }
            LOGGER.info("会话 {} 取消所有订阅", sessionId);
        }
    }

    /**
     * 向订阅了指定主题的所有会话广播消息
     * 
     * @param subject 主题名称
     * @param message 消息内容
     */
    public void broadcast(String subject, Message message) {
        if (subject == null) {
            return;
        }
        Set<String> subscribers = subjectSubscriptions.get(subject);
        if (subscribers == null || subscribers.isEmpty()) {
            LOGGER.info("主题 {} 没有订阅者, 在线会话数: {}", subject, sessions.size());
            return;
        }
        String messageJson = JSON.toJSONString(message);
        int successCount = 0;
        for (String sessionId : subscribers) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                    successCount++;
                } catch (IOException e) {
                    LOGGER.error("[广播消息异常] sessionId: {}, subject: {}", sessionId, subject, e);
                }
            }
        }
        LOGGER.info("向主题 {} 广播消息，成功发送: {}/{}", subject, successCount, subscribers.size());
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
