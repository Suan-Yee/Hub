package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages WebSocket sessions with improved tracking and lookup capabilities.
 * Provides efficient session management for high-performance scenarios.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketSessionManager {

    // Session ID -> Session Info
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    
    // Username -> Set of Session IDs (for multi-device support)
    private final Map<String, Set<String>> usernameSessions = new ConcurrentHashMap<>();
    
    // Session ID -> Set of subscribed destinations
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    /**
     * Register a new WebSocket session.
     */
    public void registerSession(String sessionId, String username) {
        if (sessionId == null) {
            return;
        }

        SessionInfo sessionInfo = new SessionInfo(sessionId, username, System.currentTimeMillis());
        sessions.put(sessionId, sessionInfo);
        
        if (username != null) {
            usernameSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
            log.debug("Session registered: {} for user: {}", sessionId, username);
        } else {
            log.debug("Session registered: {} (anonymous)", sessionId);
        }
    }

    /**
     * Remove a WebSocket session.
     */
    public void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        SessionInfo sessionInfo = sessions.remove(sessionId);
        sessionSubscriptions.remove(sessionId);
        
        if (sessionInfo != null && sessionInfo.username() != null) {
            Set<String> userSessions = usernameSessions.get(sessionInfo.username());
            if (userSessions != null) {
                userSessions.remove(sessionId);
                if (userSessions.isEmpty()) {
                    usernameSessions.remove(sessionInfo.username());
                }
            }
            log.debug("Session removed: {} for user: {}", sessionId, sessionInfo.username());
        } else {
            log.debug("Session removed: {}", sessionId);
        }
    }

    /**
     * Add a subscription to a session.
     */
    public void addSubscription(String sessionId, String destination) {
        if (sessionId != null && destination != null) {
            sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                    .add(destination);
            log.debug("Subscription added - Session: {}, Destination: {}", sessionId, destination);
        }
    }

    /**
     * Remove a subscription from a session.
     */
    public void removeSubscription(String sessionId, String subscriptionId) {
        if (sessionId != null && subscriptionId != null) {
            Set<String> subscriptions = sessionSubscriptions.get(sessionId);
            if (subscriptions != null) {
                subscriptions.removeIf(dest -> dest.contains(subscriptionId));
                log.debug("Subscription removed - Session: {}, Subscription: {}", sessionId, subscriptionId);
            }
        }
    }

    /**
     * Get all session IDs for a given username.
     */
    public Set<String> getSessionsByUsername(String username) {
        if (username == null) {
            return Collections.emptySet();
        }
        return usernameSessions.getOrDefault(username, Collections.emptySet());
    }

    /**
     * Get session info by session ID.
     */
    public Optional<SessionInfo> getSessionInfo(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Check if a user is online (has at least one active session).
     */
    public boolean isUserOnline(String username) {
        if (username == null) {
            return false;
        }
        Set<String> userSessions = usernameSessions.get(username);
        return userSessions != null && !userSessions.isEmpty();
    }

    /**
     * Get all active sessions.
     */
    public Collection<SessionInfo> getAllSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Get all online usernames.
     */
    public Set<String> getOnlineUsernames() {
        return Collections.unmodifiableSet(usernameSessions.keySet());
    }

    /**
     * Get subscriptions for a session.
     */
    public Set<String> getSessionSubscriptions(String sessionId) {
        return sessionSubscriptions.getOrDefault(sessionId, Collections.emptySet());
    }

    /**
     * Get count of active sessions.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Get count of online users.
     */
    public int getOnlineUserCount() {
        return usernameSessions.size();
    }

    /**
     * Get sessions that have been idle for longer than specified duration.
     */
    public List<SessionInfo> getIdleSessions(long idleThresholdMs) {
        long now = System.currentTimeMillis();
        return sessions.values().stream()
                .filter(session -> (now - session.connectedAt()) > idleThresholdMs)
                .collect(Collectors.toList());
    }

    /**
     * Clean up stale sessions (sessions older than threshold).
     */
    public int cleanupStaleSessions(long staleThresholdMs) {
        List<SessionInfo> staleSessions = getIdleSessions(staleThresholdMs);
        staleSessions.forEach(session -> removeSession(session.sessionId()));
        
        if (!staleSessions.isEmpty()) {
            log.info("Cleaned up {} stale sessions", staleSessions.size());
        }
        
        return staleSessions.size();
    }

    /**
     * Get session statistics.
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalSessions", sessions.size(),
            "onlineUsers", usernameSessions.size(),
            "totalSubscriptions", sessionSubscriptions.values().stream()
                    .mapToInt(Set::size)
                    .sum(),
            "averageSubscriptionsPerSession", sessions.isEmpty() ? 0 : 
                    sessionSubscriptions.values().stream()
                            .mapToInt(Set::size)
                            .average()
                            .orElse(0.0)
        );
    }

    /**
     * Session information record.
     */
    public record SessionInfo(
            String sessionId,
            String username,
            long connectedAt
    ) {
        public long getSessionDuration() {
            return System.currentTimeMillis() - connectedAt;
        }
    }
}
