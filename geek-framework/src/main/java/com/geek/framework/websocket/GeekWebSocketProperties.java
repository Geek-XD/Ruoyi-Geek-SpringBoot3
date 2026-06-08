package com.geek.framework.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@ConfigurationProperties(prefix = "geek.websocket")
public class GeekWebSocketProperties {

    @Getter(AccessLevel.NONE)
    private boolean enabled = true;
    private String path = "/websocket/message";
    private String nodeName;
    private int maxOnlineCount = 100;
    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

    public boolean isEnabled() {
        return enabled;
    }

    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
