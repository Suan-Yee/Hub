package com.example.demo.service.impl;

import com.example.demo.service.OnlineStatusService;
import com.example.demo.config.WebSocketHeartbeatHandler;
import com.example.demo.config.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineStatusServiceImpl implements OnlineStatusService, ChannelInterceptor {

    private final WebSocketSessionManager sessionManager;
    private final WebSocketHeartbeatHandler heartbeatHandler;

    // Session ID -> Username mapping
    private final ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();
    
    // Username -> Set of Session IDs (for multiple device support)
    private final ConcurrentHashMap<String, Set<String>> userToSessions = new ConcurrentHashMap<>();
    
    // Session ID -> Last activity timestamp
    private final ConcurrentHashMap<String, Instant> sessionActivity = new ConcurrentHashMap<>();

    @Override
    public void handleConnect(String sessionId, String username) {
        log.info("User connected: {} (session: {})", username, sessionId);
        
        sessionToUser.put(sessionId, username);
        userToSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionActivity.put(sessionId, Instant.now());
        
        // sessionManager.addSession(sessionId, username);
        
        log.debug("Online users count: {}", getOnlineUserCount());
    }

    @Override
    public void handleDisconnect(String sessionId) {
        String username = sessionToUser.remove(sessionId);
        if (username != null) {
            Set<String> sessions = userToSessions.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userToSessions.remove(username);
                    log.info("User disconnected: {} (last session)", username);
                } else {
                    log.info("User session disconnected: {} (remaining sessions: {})", username, sessions.size());
                }
            }
        }
        
        sessionActivity.remove(sessionId);
        sessionManager.removeSession(sessionId);
        
        log.debug("Online users count: {}", getOnlineUserCount());
    }

    @Override
    public Set<String> getOnlineUsers() {
        return Set.copyOf(userToSessions.keySet());
    }

    @Override
    public boolean isUserOnline(String username) {
        return userToSessions.containsKey(username) && !userToSessions.get(username).isEmpty();
    }

    @Override
    public int getOnlineUserCount() {
        return userToSessions.size();
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        String sessionId = accessor.getSessionId();

        switch (command) {
            case CONNECT:
                Principal principal = accessor.getUser();
                if (principal != null && sessionId != null) {
                    handleConnect(sessionId, principal.getName());
                }
                break;

            case DISCONNECT:
                if (sessionId != null) {
                    handleDisconnect(sessionId);
                }
                break;

            case SEND:
            case SUBSCRIBE:
                // Update activity timestamp
                if (sessionId != null) {
                    sessionActivity.put(sessionId, Instant.now());
                }
                
                // Check for heartbeat
                if ("/app/heartbeat".equals(accessor.getDestination()) && sessionId != null) {
                    processHeartbeat(sessionId);
                }
                break;

            default:
                break;
        }

        return message;
    }

    @Override
    public void processHeartbeat(String sessionId) {
        sessionActivity.put(sessionId, Instant.now());
        // heartbeatHandler.recordHeartbeat(sessionId);
        log.trace("Heartbeat received for session: {}", sessionId);
    }

    @Override
    public void cleanupStaleSessions() {
        Instant threshold = Instant.now().minusSeconds(300); // 5 minutes
        
        List<String> staleSessions = sessionActivity.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(threshold))
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());

        staleSessions.forEach(sessionId -> {
            log.warn("Cleaning up stale session: {}", sessionId);
            handleDisconnect(sessionId);
        });

        if (!staleSessions.isEmpty()) {
            log.info("Cleaned up {} stale sessions", staleSessions.size());
        }
    }

    @Override
    public List<String> getSessionsForUser(String username) {
        Set<String> sessions = userToSessions.get(username);
        return sessions != null ? List.copyOf(sessions) : List.of();
    }

    /**
     * Get session statistics
     */
    public String getSessionStatistics() {
        return String.format(
                "Online Users: %d, Total Sessions: %d, Active Sessions: %d",
                getOnlineUserCount(),
                sessionToUser.size(),
                sessionActivity.size()
        );
    }

    /**
     * Force disconnect a user (all sessions)
     */
    public void forceDisconnectUser(String username) {
        Set<String> sessions = userToSessions.get(username);
        if (sessions != null) {
            sessions.forEach(this::handleDisconnect);
            log.info("Force disconnected user: {}", username);
        }
    }
}
