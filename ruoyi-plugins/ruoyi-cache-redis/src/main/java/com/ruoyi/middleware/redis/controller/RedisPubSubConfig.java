package com.ruoyi.middleware.redis.controller;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ruoyi.middleware.redis.annotation.RedisListener;

@Configuration
public class RedisPubSubConfig {

    @Bean("redisListenerExecutor")
    public Executor redisListenerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 核心线程数
        executor.setMaxPoolSize(10); // 最大线程数（控制最大并发）
        executor.setQueueCapacity(100); // 等待队列大小
        executor.setThreadNamePrefix("redis-listener-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy()); // 拒绝策略
        executor.initialize();
        return executor;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            List<MessageListener> redisMessageListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.setTaskExecutor(redisListenerExecutor());
        redisMessageListener.stream()
                .map(listener -> new SimpleImmutableEntry<>(listener,
                        listener.getClass().getAnnotation(RedisListener.class)))
                .filter(sim -> sim.getValue() != null)
                .forEach(sim -> {
                    String channel = sim.getValue().value();
                    container.addMessageListener(sim.getKey(), new PatternTopic(channel));
                });
        return container;
    }
}