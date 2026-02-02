package com.example.demo.presentation.rest;

import com.example.demo.application.usecase.OnlineStatusService;
import com.example.demo.config.WebSocketEventListener;
import com.example.demo.config.WebSocketHeartbeatHandler;
import com.example.demo.config.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for WebSocket metrics and monitoring.
 * Provides insights into connection health, session statistics, and online users.
 * Restricted to ADMIN role.
 */
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebSocketMetricsController {

    private final OnlineStatusService onlineStatusService;
    private final WebSocketSessionManager sessionManager;
    private final WebSocketHeartbeatHandler heartbeatHandler;
    private final WebSocketEventListener eventListener;

    /**
     * Get comprehensive WebSocket metrics.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Connection metrics
        metrics.put("connections", eventListener.getMetrics());
        
        // Session metrics
        metrics.put("sessions", sessionManager.getStatistics());
        
        // Online status metrics
        metrics.put("onlineStatus", onlineStatusService.getStatistics());
        
        // Heartbeat metrics
        metrics.put("heartbeat", Map.of(
            "activeHeartbeats", heartbeatHandler.getActiveSessionCount()
        ));
        
        // Timestamp
        metrics.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get list of online users.
     */
    @GetMapping("/online-users")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        Set<String> onlineStaffIds = onlineStatusService.getOnlineStaffIds();
        Set<String> onlineUsernames = sessionManager.getOnlineUsernames();
        
        Map<String, Object> response = Map.of(
            "onlineStaffIds", onlineStaffIds,
            "onlineUsernames", onlineUsernames,
            "count", onlineStaffIds.size(),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active sessions information.
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        var sessions = sessionManager.getAllSessions();
        
        Map<String, Object> response = Map.of(
            "sessions", sessions,
            "totalSessions", sessions.size(),
            "onlineUsers", sessionManager.getOnlineUserCount(),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get health check for WebSocket service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        int activeSessions = sessionManager.getActiveSessionCount();
        int activeHeartbeats = heartbeatHandler.getActiveSessionCount();
        
        boolean healthy = activeHeartbeats >= activeSessions * 0.8; // At least 80% healthy sessions
        
        Map<String, Object> health = Map.of(
            "status", healthy ? "UP" : "DEGRADED",
            "activeSessions", activeSessions,
            "activeHeartbeats", activeHeartbeats,
            "healthPercentage", activeSessions > 0 ? 
                    (double) activeHeartbeats / activeSessions * 100 : 100,
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(health);
    }

    /**
     * Trigger cleanup of stale sessions manually (POST; side-effecting).
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupStaleSessions() {
        int cleanedCount = onlineStatusService.cleanupStaleSessions();
        
        Map<String, Object> response = Map.of(
            "cleanedSessions", cleanedCount,
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(response);
    }
}
