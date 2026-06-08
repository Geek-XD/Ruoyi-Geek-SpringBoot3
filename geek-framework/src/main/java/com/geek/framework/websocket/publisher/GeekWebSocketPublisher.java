package com.geek.framework.websocket.publisher;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import com.geek.common.core.domain.Message;
import com.geek.common.enums.MessageType;
import com.geek.framework.websocket.registry.GeekWebSocketSessionRegistry;
import com.geek.common.utils.StringUtils;

@Component
public class GeekWebSocketPublisher implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeekWebSocketPublisher.class);

    private final GeekWebSocketSessionRegistry sessionRegistry;
    private final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "geek-websocket-debounce");
        thread.setDaemon(true);
        return thread;
    });
    private final ConcurrentMap<String, DebounceEntry> debounceEntries = new ConcurrentHashMap<>();

    public GeekWebSocketPublisher(GeekWebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void publish(String subject, Map<String, Object> payload) {
        publish(Message.builder()
                .sender("system")
                .type(MessageType.EVENT)
                .subject(subject)
                .content("broadcast")
                .payload(payload)
                .build());
    }

    public void publish(Message message) {
        Message normalized = normalize(message);
        LOGGER.info("WebSocket 立即发布 - subject={}, payloadKeys={}",
                normalized.getSubject(),
                normalized.getPayload() == null ? java.util.Set.of() : normalized.getPayload().keySet());
        sessionRegistry.broadcast(normalized.getSubject(), normalized);
    }

    public void publishDebounced(Message message, long debounceMillis) {
        Message normalized = normalize(message);
        if (debounceMillis <= 0L) {
            publish(normalized);
            return;
        }
        LOGGER.info("WebSocket 防抖发布已入队 - subject={}, debounceMs={}, payloadKeys={}",
                normalized.getSubject(),
                debounceMillis,
                normalized.getPayload() == null ? java.util.Set.of() : normalized.getPayload().keySet());
        debounceEntries.compute(normalized.getSubject(), (subject, existing) -> {
            DebounceEntry entry = existing == null ? new DebounceEntry() : existing;
            entry.message = normalized;
            if (entry.future != null) {
                entry.future.cancel(false);
            }
            entry.future = debounceExecutor.schedule(() -> flushDebounced(subject), debounceMillis,
                    TimeUnit.MILLISECONDS);
            return entry;
        });
    }

    private void flushDebounced(String subject) {
        DebounceEntry entry = debounceEntries.remove(subject);
        if (entry == null || entry.message == null) {
            return;
        }
        publish(entry.message);
    }

    private Message normalize(Message message) {
        Message source = Objects.requireNonNull(message, "WebSocket message must not be null");
        if (StringUtils.isEmpty(source.getSubject())) {
            throw new IllegalArgumentException("WebSocket message subject must not be empty");
        }
        if (source.getType() == null) {
            source.setType(MessageType.EVENT);
        }
        if (StringUtils.isEmpty(source.getContent())) {
            source.setContent("broadcast");
        }
        return source;
    }

    @Override
    public void destroy() {
        debounceExecutor.shutdownNow();
        debounceEntries.clear();
    }

    private static final class DebounceEntry {
        private volatile Message message;
        private volatile ScheduledFuture<?> future;
    }
}
