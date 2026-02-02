package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles WebSocket heartbeat (ping/pong) to detect stale connections.
 * Automatically manages connection health monitoring and timeout handling.
 */
@Component
@Slf4j
public class WebSocketHeartbeatHandler implements ChannelInterceptor {

    private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> missedHeartbeatMap = new ConcurrentHashMap<>();
    
    private static final long HEARTBEAT_INTERVAL_MS = 20_000; // 20 seconds
    private static final int MAX_MISSED_HEARTBEATS = 3; // Disconnect after 3 missed heartbeats (60 seconds)
    
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "WebSocket-Heartbeat-Monitor");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void init() {
        // Start heartbeat monitoring after bean construction
        startHeartbeatMonitoring();
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            String sessionId = accessor.getSessionId();
            StompCommand command = accessor.getCommand();

            if (command != null) {
                switch (command) {
                    case CONNECT:
                        handleConnect(sessionId);
                        break;
                    case DISCONNECT:
                        handleDisconnect(sessionId);
                        break;
                    case SEND:
                    case SUBSCRIBE:
                        // Any activity resets the heartbeat timer
                        recordHeartbeat(sessionId);
                        break;
                    default:
                        break;
                }
            }
        }

        return message;
    }

    private void handleConnect(String sessionId) {
        if (sessionId != null) {
            recordHeartbeat(sessionId);
            missedHeartbeatMap.put(sessionId, 0);
            log.debug("Heartbeat tracking started for session: {}", sessionId);
        }
    }

    private void handleDisconnect(String sessionId) {
        if (sessionId != null) {
            lastHeartbeatMap.remove(sessionId);
            missedHeartbeatMap.remove(sessionId);
            log.debug("Heartbeat tracking stopped for session: {}", sessionId);
        }
    }

    private void recordHeartbeat(String sessionId) {
        if (sessionId != null) {
            lastHeartbeatMap.put(sessionId, System.currentTimeMillis());
            missedHeartbeatMap.put(sessionId, 0);
        }
    }

    private void startHeartbeatMonitoring() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                checkHeartbeats();
            } catch (Exception e) {
                log.error("Error in heartbeat monitoring", e);
            }
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        
        log.info("WebSocket heartbeat monitoring started (interval: {}ms, max missed: {})", 
                HEARTBEAT_INTERVAL_MS, MAX_MISSED_HEARTBEATS);
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        
        lastHeartbeatMap.forEach((sessionId, lastHeartbeat) -> {
            long timeSinceLastHeartbeat = now - lastHeartbeat;
            
            if (timeSinceLastHeartbeat > HEARTBEAT_INTERVAL_MS) {
                int missedCount = missedHeartbeatMap.getOrDefault(sessionId, 0) + 1;
                missedHeartbeatMap.put(sessionId, missedCount);
                
                if (missedCount >= MAX_MISSED_HEARTBEATS) {
                    log.warn("Session {} has missed {} heartbeats ({}ms). Connection likely stale.", 
                            sessionId, missedCount, timeSinceLastHeartbeat);
                    // Mark for cleanup - actual disconnect handled by WebSocketSessionManager
                    lastHeartbeatMap.remove(sessionId);
                    missedHeartbeatMap.remove(sessionId);
                } else {
                    log.debug("Session {} missed heartbeat #{} ({}ms ago)", 
                            sessionId, missedCount, timeSinceLastHeartbeat);
                }
            }
        });
    }

    /**
     * Check if a session is healthy (receiving heartbeats).
     */
    public boolean isSessionHealthy(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        Long lastHeartbeat = lastHeartbeatMap.get(sessionId);
        if (lastHeartbeat == null) {
            return false;
        }
        
        int missedCount = missedHeartbeatMap.getOrDefault(sessionId, 0);
        return missedCount < MAX_MISSED_HEARTBEATS;
    }

    /**
     * Get the number of active sessions being monitored.
     */
    public int getActiveSessionCount() {
        return lastHeartbeatMap.size();
    }

    /**
     * Shutdown the heartbeat monitor when the bean is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        heartbeatScheduler.shutdown();
        try {
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
