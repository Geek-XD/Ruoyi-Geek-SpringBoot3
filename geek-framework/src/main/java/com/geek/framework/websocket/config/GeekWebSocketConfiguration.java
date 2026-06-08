package com.geek.framework.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.geek.framework.websocket.transport.GeekWebSocketHandler;
import com.geek.framework.websocket.transport.GeekWebSocketHandshakeInterceptor;

@Configuration
@EnableWebSocket
@EnableConfigurationProperties(GeekWebSocketProperties.class)
@ConditionalOnProperty(prefix = "geek.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GeekWebSocketConfiguration implements WebSocketConfigurer {

    private final GeekWebSocketHandler webSocketHandler;
    private final GeekWebSocketHandshakeInterceptor handshakeInterceptor;
    private final GeekWebSocketProperties properties;

    public GeekWebSocketConfiguration(GeekWebSocketHandler webSocketHandler,
            GeekWebSocketHandshakeInterceptor handshakeInterceptor, GeekWebSocketProperties properties) {
        this.webSocketHandler = webSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
        this.properties = properties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, properties.getPath())
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(properties.allowedOriginsArray());
    }
}
