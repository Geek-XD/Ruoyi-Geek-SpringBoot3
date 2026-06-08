package com.geek.framework.websocket.metadata;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeekWebSocketSessionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String nodeName;
    private String username;
    private Long userId;
    private String uri;
    private String remoteAddress;
    private long connectedAt;
}
