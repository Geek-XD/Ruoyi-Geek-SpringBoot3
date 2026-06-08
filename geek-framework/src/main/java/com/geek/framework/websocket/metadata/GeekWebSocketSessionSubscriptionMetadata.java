package com.geek.framework.websocket.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeekWebSocketSessionSubscriptionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nodeName;
    private String sessionId;
    @Builder.Default
    private Set<String> subjects = new LinkedHashSet<>();
}
