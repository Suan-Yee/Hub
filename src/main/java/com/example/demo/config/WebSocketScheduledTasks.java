package com.example.demo.config;

import com.example.demo.application.usecase.impl.OnlineStatusServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Scheduled tasks for WebSocket maintenance and monitoring.
 * Handles periodic cleanup of stale sessions and health monitoring.
 */
@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class WebSocketScheduledTasks {

    private final OnlineStatusServiceImpl onlineStatusService;
    private final WebSocketSessionManager sessionManager;
    private final WebSocketProperties properties;

    /**
     * Cleanup stale sessions periodically.
     * Runs every 5 minutes by default (configurable via properties).
     */
    @Scheduled(fixedDelayString = "${websocket.stale-session-cleanup-interval:300000}")
    public void cleanupStaleSessions() {
        try {
            int onlineStatusCleanedCount = onlineStatusService.cleanupStaleSessions();
            int sessionManagerCleanedCount = sessionManager.cleanupStaleSessions(
                    properties.getSessionIdleTimeout()
            );
            
            int totalCleaned = onlineStatusCleanedCount + sessionManagerCleanedCount;
            
            if (totalCleaned > 0) {
                log.info("Stale session cleanup completed - Removed {} sessions", totalCleaned);
            } else {
                log.debug("Stale session cleanup completed - No stale sessions found");
            }
        } catch (Exception e) {
            log.error("Error during stale session cleanup", e);
        }
    }

    /**
     * Log WebSocket statistics periodically for monitoring.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void logWebSocketStatistics() {
        try {
            var stats = onlineStatusService.getStatistics();
            var sessionStats = sessionManager.getStatistics();
            
            log.info("WebSocket Statistics - Online users: {}, Total sessions: {}, Healthy sessions: {}, Stale sessions: {}",
                    stats.get("onlineUsers"),
                    stats.get("totalSessions"),
                    stats.get("healthySessions"),
                    stats.get("staleSessions"));
            
            log.debug("Session Manager Statistics - {}", sessionStats);
        } catch (Exception e) {
            log.error("Error logging WebSocket statistics", e);
        }
    }

    /**
     * Perform health check on WebSocket connections.
     * Runs every 30 seconds to detect issues early.
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void performHealthCheck() {
        try {
            var stats = onlineStatusService.getStatistics();
            int totalSessions = (int) stats.get("totalSessions");
            int healthySessions = (int) stats.get("healthySessions");
            int staleSessions = (int) stats.get("staleSessions");
            
            // Alert if more than 20% of sessions are stale
            if (totalSessions > 0) {
                double stalePercentage = (double) staleSessions / totalSessions * 100;
                if (stalePercentage > 20) {
                    log.warn("High percentage of stale sessions detected: {:.2f}% ({}/{})",
                            stalePercentage, staleSessions, totalSessions);
                }
            }
        } catch (Exception e) {
            log.error("Error during WebSocket health check", e);
        }
    }
}
