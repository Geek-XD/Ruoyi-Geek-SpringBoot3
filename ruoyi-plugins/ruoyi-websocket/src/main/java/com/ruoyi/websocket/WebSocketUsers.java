package com.ruoyi.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.R;

/**
 * websocket 客户端用户集
 * 
 * @author ruoyi
 */
public class WebSocketUsers {
    /**
     * WebSocketUsers 日志控制器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUsers.class);

    /**
     * 用户集
     */
    private static Map<String, WebSocketSession> USERS = new ConcurrentHashMap<String, WebSocketSession>();

    /**
     * 存储用户
     *
     * @param key     唯一键
     * @param session 用户信息
     */
    public static void put(String key, WebSocketSession session) {
        USERS.put(key, session);
    }

    /**
     * 移除用户
     *
     * @param session 用户信息
     *
     * @return 移除结果
     */
    public static boolean remove(WebSocketSession session) {
        String key = null;
        boolean flag = USERS.containsValue(session);
        if (flag) {
            Set<Map.Entry<String, WebSocketSession>> entries = USERS.entrySet();
            for (Map.Entry<String, WebSocketSession> entry : entries) {
                WebSocketSession value = entry.getValue();
                if (value.equals(session)) {
                    key = entry.getKey();
                    break;
                }
            }
        } else {
            return true;
        }
        return remove(key);
    }

    /**
     * 移出用户
     *
     * @param key 键
     */
    public static boolean remove(String key) {
        LOGGER.info("\n 正在移出用户 - {}", key);
        WebSocketSession remove = USERS.remove(key);
        if (remove != null) {
            boolean containsValue = USERS.containsValue(remove);
            LOGGER.info("\n 移出结果 - {}", containsValue ? "失败" : "成功");
            return containsValue;
        } else {
            return true;
        }
    }

    /**
     * 获取在线用户列表
     *
     * @return 返回用户集合
     */
    public static Map<String, WebSocketSession> getUsers() {
        return USERS;
    }

    /**
     * 群发消息文本消息
     *
     * @param message 消息内容
     */
    public static void sendMessageToUsersByText(String message) {
        Collection<WebSocketSession> values = USERS.values();
        for (WebSocketSession value : values) {
            sendMessageToUserByText(value, message);
        }
    }

    /**
     * 发送文本消息
     *
     * @param session WebSocket会话
     * @param message 消息内容
     */
    public static void sendMessageToUserByText(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                R<String> r = R.ok(message);
                session.sendMessage(new TextMessage(JSONObject.toJSONString(r)));
            } catch (IOException e) {
                LOGGER.error("\n[发送消息异常]", e);
            }
        } else {
            LOGGER.info("\n[连接已断开或不存在]");
        }
    }
}
