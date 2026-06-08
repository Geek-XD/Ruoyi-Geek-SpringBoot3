package com.geek.framework.websocket;

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
public class GeekWebSocketSubjectSubscriptionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nodeName;
    private String subject;
    @Builder.Default
    private Set<String> sessionIds = new LinkedHashSet<>();
}
