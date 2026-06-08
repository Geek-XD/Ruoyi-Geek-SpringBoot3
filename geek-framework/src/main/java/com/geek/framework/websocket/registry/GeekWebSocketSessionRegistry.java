package com.geek.framework.websocket.registry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import com.geek.common.constant.CacheConstants;
import com.geek.common.core.cache.GeekCacheManager;
import com.geek.common.core.domain.Message;
import com.geek.common.core.domain.model.LoginUser;
import com.geek.common.utils.JSON;
import com.geek.common.utils.StringUtils;
import com.geek.framework.websocket.config.GeekWebSocketProperties;
import com.geek.framework.websocket.metadata.GeekWebSocketNodeMetadata;
import com.geek.framework.websocket.metadata.GeekWebSocketSessionMetadata;
import com.geek.framework.websocket.metadata.GeekWebSocketSessionSubscriptionMetadata;
import com.geek.framework.websocket.metadata.GeekWebSocketSubjectSubscriptionMetadata;

import jakarta.annotation.PreDestroy;

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
    private final GeekCacheManager cacheManager;
    private final String nodeName;
    private final long startedAt;

    public GeekWebSocketSessionRegistry(GeekWebSocketProperties properties, GeekCacheManager cacheManager,
            Environment environment) {
        this.properties = properties;
        this.cacheManager = cacheManager;
        this.socketSemaphore = new Semaphore(properties.getMaxOnlineCount());
        this.nodeName = resolveNodeName(properties, environment);
        this.startedAt = System.currentTimeMillis();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeNodeMetadata() {
        resetCurrentNodeCacheState();
        refreshNodeMetadata();
        LOGGER.info("WebSocket 节点缓存初始化完成 - nodeName={}", nodeName);
    }

    public void register(WebSocketSession session, LoginUser loginUser) {
        if (!socketSemaphore.tryAcquire()) {
            throw new IllegalStateException("当前在线人数超过限制数：" + properties.getMaxOnlineCount());
        }
        try {
            sessions.put(session.getId(), session);
            if (loginUser != null) {
                loginUsers.put(session.getId(), loginUser);
                usernameSessions.put(loginUser.getUsername(), session);
            }
            cacheSessionMetadata(session, loginUser);
            refreshNodeMetadata();
        } catch (RuntimeException e) {
            sessions.remove(session.getId());
            if (loginUser != null) {
                loginUsers.remove(session.getId());
                usernameSessions.remove(loginUser.getUsername(), session);
            }
            socketSemaphore.release();
            throw new IllegalStateException("WebSocket 会话缓存注册失败", e);
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
        removeSessionMetadata(sessionId);
        refreshNodeMetadataQuietly();
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
        refreshSubjectSubscriptionMetadata(subject);
        refreshSessionSubscriptionMetadata(sessionId);
        refreshNodeMetadataQuietly();
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
        refreshSubjectSubscriptionMetadata(subject);
        refreshSessionSubscriptionMetadata(sessionId);
        refreshNodeMetadataQuietly();
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
                refreshSubjectSubscriptionMetadata(subject);
            }
            removeSessionSubscriptionMetadata(sessionId);
            refreshNodeMetadataQuietly();
            LOGGER.info("会话 {} 取消所有订阅", sessionId);
        } else {
            removeSessionSubscriptionMetadata(sessionId);
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

    @PreDestroy
    public void destroy() {
        clearCurrentNodeCacheState();
    }

    private void cacheSessionMetadata(WebSocketSession session, LoginUser loginUser) {
        GeekWebSocketSessionMetadata metadata = GeekWebSocketSessionMetadata.builder()
                .sessionId(session.getId())
                .nodeName(nodeName)
                .username(loginUser == null ? null : loginUser.getUsername())
                .userId(loginUser == null ? null : loginUser.getUserId())
                .uri(toString(session.getUri()))
                .remoteAddress(session.getRemoteAddress() == null ? null : session.getRemoteAddress().toString())
                .connectedAt(System.currentTimeMillis())
                .build();
        cacheManager.put(CacheConstants.WEBSOCKET_SESSION_KEY, buildSessionCacheKey(session.getId()), metadata);
    }

    private void removeSessionMetadata(String sessionId) {
        cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_KEY, buildSessionCacheKey(sessionId));
    }

    private void refreshNodeMetadata() {
        GeekWebSocketNodeMetadata metadata = GeekWebSocketNodeMetadata.builder()
                .nodeName(nodeName)
                .startedAt(startedAt)
                .onlineCount(sessions.size())
                .sessionIds(new LinkedHashSet<>(sessions.keySet()))
                .subjects(new LinkedHashSet<>(subjectSubscriptions.keySet()))
                .build();
        cacheManager.put(CacheConstants.WEBSOCKET_NODE_KEY, nodeName, metadata);
    }

    private void refreshNodeMetadataQuietly() {
        try {
            refreshNodeMetadata();
        } catch (RuntimeException e) {
            LOGGER.warn("刷新 WebSocket 节点缓存描述失败 - nodeName={}", nodeName, e);
        }
    }

    private void resetCurrentNodeCacheState() {
        GeekWebSocketNodeMetadata previousNode = cacheManager.get(CacheConstants.WEBSOCKET_NODE_KEY, nodeName,
                GeekWebSocketNodeMetadata.class);
        if (previousNode != null) {
            if (previousNode.getSessionIds() != null) {
                for (String sessionId : previousNode.getSessionIds()) {
                    cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_KEY, buildSessionCacheKey(sessionId));
                    cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_SUBSCRIPTION_KEY,
                            buildSessionSubscriptionCacheKey(sessionId));
                }
            }
            if (previousNode.getSubjects() != null) {
                for (String subject : previousNode.getSubjects()) {
                    cacheManager.remove(CacheConstants.WEBSOCKET_SUBJECT_SUBSCRIPTION_KEY,
                            buildSubjectSubscriptionCacheKey(subject));
                }
            }
        }
        cacheManager.remove(CacheConstants.WEBSOCKET_NODE_KEY, nodeName);
    }

    private void clearCurrentNodeCacheState() {
        Set<String> currentSessionIds = new LinkedHashSet<>(sessions.keySet());
        Set<String> currentSubjects = new LinkedHashSet<>(subjectSubscriptions.keySet());
        for (String sessionId : currentSessionIds) {
            cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_KEY, buildSessionCacheKey(sessionId));
            cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_SUBSCRIPTION_KEY,
                    buildSessionSubscriptionCacheKey(sessionId));
        }
        for (String subject : currentSubjects) {
            cacheManager.remove(CacheConstants.WEBSOCKET_SUBJECT_SUBSCRIPTION_KEY,
                    buildSubjectSubscriptionCacheKey(subject));
        }
        cacheManager.remove(CacheConstants.WEBSOCKET_NODE_KEY, nodeName);
        LOGGER.info("清理 WebSocket 节点缓存描述完成 - nodeName={}, sessionCount={}", nodeName, currentSessionIds.size());
    }

    private String buildSessionCacheKey(String sessionId) {
        return nodeName + ":" + sessionId;
    }

    private String buildSubjectSubscriptionCacheKey(String subject) {
        return nodeName + ":" + subject;
    }

    private String buildSessionSubscriptionCacheKey(String sessionId) {
        return nodeName + ":" + sessionId;
    }

    private void refreshSubjectSubscriptionMetadata(String subject) {
        if (subject == null) {
            return;
        }
        Set<String> sessionIds = subjectSubscriptions.get(subject);
        if (sessionIds == null || sessionIds.isEmpty()) {
            cacheManager.remove(CacheConstants.WEBSOCKET_SUBJECT_SUBSCRIPTION_KEY,
                    buildSubjectSubscriptionCacheKey(subject));
            return;
        }
        GeekWebSocketSubjectSubscriptionMetadata metadata = GeekWebSocketSubjectSubscriptionMetadata.builder()
                .nodeName(nodeName)
                .subject(subject)
                .sessionIds(new LinkedHashSet<>(sessionIds))
                .build();
        cacheManager.put(CacheConstants.WEBSOCKET_SUBJECT_SUBSCRIPTION_KEY,
                buildSubjectSubscriptionCacheKey(subject), metadata);
    }

    private void refreshSessionSubscriptionMetadata(String sessionId) {
        if (sessionId == null) {
            return;
        }
        Set<String> subjects = sessionSubscriptions.get(sessionId);
        if (subjects == null || subjects.isEmpty()) {
            removeSessionSubscriptionMetadata(sessionId);
            return;
        }
        GeekWebSocketSessionSubscriptionMetadata metadata = GeekWebSocketSessionSubscriptionMetadata.builder()
                .nodeName(nodeName)
                .sessionId(sessionId)
                .subjects(new LinkedHashSet<>(subjects))
                .build();
        cacheManager.put(CacheConstants.WEBSOCKET_SESSION_SUBSCRIPTION_KEY,
                buildSessionSubscriptionCacheKey(sessionId), metadata);
    }

    private void removeSessionSubscriptionMetadata(String sessionId) {
        if (sessionId == null) {
            return;
        }
        cacheManager.remove(CacheConstants.WEBSOCKET_SESSION_SUBSCRIPTION_KEY,
                buildSessionSubscriptionCacheKey(sessionId));
    }

    private String resolveNodeName(GeekWebSocketProperties properties, Environment environment) {
        if (StringUtils.isNotEmpty(properties.getNodeName())) {
            return properties.getNodeName();
        }
        String appName = environment.getProperty("spring.application.name", "application");
        if (StringUtils.isNotEmpty(appName)) {
            return appName;
        }
        return resolveHostName();
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOGGER.warn("获取主机名失败，WebSocket 节点名将使用 unknown-host", e);
            return "unknown-host";
        }
    }

    private String toString(URI uri) {
        return uri == null ? null : uri.toString();
    }
}
