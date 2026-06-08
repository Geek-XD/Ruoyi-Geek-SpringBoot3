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
public class GeekWebSocketNodeMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nodeName;
    private long startedAt;
    private int onlineCount;
    @Builder.Default
    private Set<String> sessionIds = new LinkedHashSet<>();
    @Builder.Default
    private Set<String> subjects = new LinkedHashSet<>();
}
