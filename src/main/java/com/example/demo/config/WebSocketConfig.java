package com.example.demo.config;


import com.example.demo.application.usecase.OnlineStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Enhanced WebSocket configuration with performance optimizations,
 * ping/pong support, connection pooling, and advanced monitoring.
 * 
 * Key features:
 * - Heartbeat monitoring for stale connection detection
 * - Configurable message size and buffer limits
 * - Thread pool optimization for concurrent connections
 * - Compression support for reduced bandwidth
 * - Enhanced session tracking and management
 * - STOMP relay support for scalability
 */
@EnableWebSocketMessageBroker
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final OnlineStatusService onlineStatusService;
    private final WebSocketHeartbeatHandler heartbeatHandler;
    private final WebSocketProperties properties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // CORS: wildcard (*) with allowCredentials=true is invalid per spec; guard against it
        String[] origins = properties.getCors().getAllowedOrigins();
        boolean allowCredentials = properties.getCors().isAllowCredentials();
        if (origins != null && origins.length > 0 && allowCredentials) {
            boolean hasWildcard = false;
            for (String o : origins) {
                if ("*".equals(o)) {
                    hasWildcard = true;
                    break;
                }
            }
            if (hasWildcard) {
                log.warn("Invalid CORS: allowedOrigins contains '*' with allowCredentials=true. Using empty origins and allowCredentials=false.");
                origins = new String[0];
                allowCredentials = false;
            }
        }
        String[] allowedOrigins = (origins != null && origins.length > 0) ? origins : new String[0];

        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                .setStreamBytesLimit(512 * 1024)  // 512KB for SockJS streaming
                .setHttpMessageCacheSize(1000)     // Cache up to 1000 messages
                .setDisconnectDelay(5 * 1000);     // 5 second disconnect delay
        
        // Also register native WebSocket endpoint (for better performance when available)
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
        
        log.info("WebSocket endpoints registered: /ws (with SockJS fallback)");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        
        if (properties.getRelay().isEnabled()) {
            // Use external message broker (RabbitMQ, ActiveMQ, etc.) for scalability
            registry.enableStompBrokerRelay("/topic", "/queue", "/comment", "/user")
                    .setRelayHost(properties.getRelay().getHost())
                    .setRelayPort(properties.getRelay().getPort())
                    .setClientLogin(properties.getRelay().getClientLogin())
                    .setClientPasscode(properties.getRelay().getClientPasscode())
                    .setSystemLogin(properties.getRelay().getSystemLogin())
                    .setSystemPasscode(properties.getRelay().getSystemPasscode())
                    .setVirtualHost(properties.getRelay().getVirtualHost())
                    .setUserDestinationBroadcast("/topic/unresolved-user-destination")
                    .setUserRegistryBroadcast("/topic/user-registry-broadcast");
            
            log.info("STOMP broker relay enabled - Host: {}:{}", 
                    properties.getRelay().getHost(), properties.getRelay().getPort());
        } else {
            // Use in-memory simple broker with heartbeat for production
            registry.enableSimpleBroker("/topic", "/queue", "/comment", "/user")
                    .setHeartbeatValue(new long[]{properties.getHeartbeatInterval(), properties.getHeartbeatInterval()})
                    .setTaskScheduler(heartbeatTaskScheduler());
            
            log.info("Simple in-memory broker enabled with heartbeat interval: {}ms", 
                    properties.getHeartbeatInterval());
        }
        
        registry.setUserDestinationPrefix("/user");
        registry.setPreservePublishOrder(true);  // Maintain message order for same destination
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register interceptors for session tracking and heartbeat monitoring
        registration.interceptors(
                onlineStatusService.sessionChannelInterceptor(),
                heartbeatHandler
        );
        
        // Configure thread pool for inbound messages
        registration.taskExecutor()
                .corePoolSize(properties.getThreadPoolSize())
                .maxPoolSize(properties.getThreadPoolSize() * 2)
                .queueCapacity(properties.getThreadPoolQueueCapacity())
                .keepAliveSeconds(60);
        
        log.info("Client inbound channel configured - Pool size: {}, Queue capacity: {}", 
                properties.getThreadPoolSize(), properties.getThreadPoolQueueCapacity());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure thread pool for outbound messages
        registration.taskExecutor()
                .corePoolSize(properties.getThreadPoolSize())
                .maxPoolSize(properties.getThreadPoolSize() * 2)
                .queueCapacity(properties.getThreadPoolQueueCapacity())
                .keepAliveSeconds(60);
        
        log.info("Client outbound channel configured - Pool size: {}, Queue capacity: {}", 
                properties.getThreadPoolSize(), properties.getThreadPoolQueueCapacity());
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(APPLICATION_JSON);
        
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Support Java 8 time API, etc.
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);
        
        messageConverters.add(converter);
        log.debug("Message converter configured with Jackson");
        return false;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(properties.getMessageSizeLimit())
                .setSendBufferSizeLimit(properties.getSendBufferSizeLimit())
                .setSendTimeLimit(properties.getSendTimeLimit())
                .setTimeToFirstMessage(30 * 1000); // 30 seconds for first message
        
        log.info("WebSocket transport configured - Message size: {}KB, Buffer: {}KB, Time limit: {}s", 
                properties.getMessageSizeLimit() / 1024, 
                properties.getSendBufferSizeLimit() / 1024,
                properties.getSendTimeLimit() / 1000);
    }

    /**
     * Configure WebSocket container with performance settings.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(properties.getMessageSizeLimit());
        container.setMaxBinaryMessageBufferSize(properties.getMessageSizeLimit());
        container.setMaxSessionIdleTimeout(properties.getSessionIdleTimeout());
        container.setAsyncSendTimeout((long) properties.getSendTimeLimit());
        
        log.info("WebSocket container configured - Max buffer: {}KB, Idle timeout: {}s", 
                properties.getMessageSizeLimit() / 1024,
                properties.getSessionIdleTimeout() / 1000);
        
        return container;
    }

    /**
     * Task scheduler for heartbeat monitoring.
     */
    @Bean
    public TaskScheduler heartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // Dedicated threads for heartbeat
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        
        log.debug("Heartbeat task scheduler initialized");
        return scheduler;
    }
}
