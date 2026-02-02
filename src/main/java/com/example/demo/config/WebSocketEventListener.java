package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced WebSocket event listener for monitoring connection lifecycle,
 * tracking metrics, and handling connection events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    
    // Metrics tracking
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalDisconnections = new AtomicInteger(0);
    private final AtomicInteger failedConnections = new AtomicInteger(0);
    private final Map<String, Long> connectionTimestamps = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = getUsername(headerAccessor);
        
        totalConnections.incrementAndGet();
        int currentActive = activeConnections.incrementAndGet();
        connectionTimestamps.put(sessionId, System.currentTimeMillis());
        
        log.info("New WebSocket connection established - Session: {}, User: {}, Active connections: {}", 
                sessionId, username, currentActive);
        
        // Broadcast user online status if username is available
        if (username != null) {
            Map<String, Object> statusUpdate = Map.of(
                "type", "USER_ONLINE",
                "username", username,
                "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = getUsername(headerAccessor);
        
        totalDisconnections.incrementAndGet();
        int currentActive = activeConnections.decrementAndGet();
        
        Long connectionTime = connectionTimestamps.remove(sessionId);
        long duration = connectionTime != null ? 
                (System.currentTimeMillis() - connectionTime) / 1000 : 0;
        
        log.info("WebSocket connection closed - Session: {}, User: {}, Active connections: {}, Duration: {}s, Close status: {}", 
                sessionId, username, currentActive, duration, event.getCloseStatus());
        
        // Clean up session
        sessionManager.removeSession(sessionId);
        
        // Broadcast user offline status if username is available
        if (username != null) {
            Map<String, Object> statusUpdate = Map.of(
                "type", "USER_OFFLINE",
                "username", username,
                "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String username = getUsername(headerAccessor);
        
        log.debug("WebSocket subscription - Session: {}, User: {}, Destination: {}", 
                sessionId, username, destination);
        
        sessionManager.addSubscription(sessionId, destination);
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        String username = getUsername(headerAccessor);
        
        log.debug("WebSocket unsubscribe - Session: {}, User: {}, Subscription: {}", 
                sessionId, username, subscriptionId);
        
        sessionManager.removeSubscription(sessionId, subscriptionId);
    }

    @EventListener
    public void handleWebSocketConnectErrorListener(AbstractSubProtocolEvent event) {
        if (event instanceof SessionConnectEvent connectEvent) {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(connectEvent.getMessage());
            String sessionId = headerAccessor.getSessionId();
            
            failedConnections.incrementAndGet();
            
            log.error("WebSocket connection error - Session: {}, Message: {}", 
                    sessionId, event.getMessage());
        }
    }

    private String getUsername(StompHeaderAccessor headerAccessor) {
        var principal = headerAccessor.getUser();
        return principal != null ? principal.getName() : null;
    }

    /**
     * Get current WebSocket metrics.
     */
    public Map<String, Object> getMetrics() {
        return Map.of(
            "totalConnections", totalConnections.get(),
            "activeConnections", activeConnections.get(),
            "totalDisconnections", totalDisconnections.get(),
            "failedConnections", failedConnections.get(),
            "activeSessions", sessionManager.getActiveSessionCount()
        );
    }

    /**
     * Reset metrics (useful for monitoring/testing).
     */
    public void resetMetrics() {
        totalConnections.set(0);
        totalDisconnections.set(0);
        failedConnections.set(0);
        // Don't reset activeConnections as it represents current state
        log.info("WebSocket metrics reset");
    }
}
