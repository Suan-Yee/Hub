package com.example.demo.application.usecase.impl;

import com.example.demo.application.usecase.OnlineStatusService;
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced implementation of OnlineStatusService with heartbeat monitoring
 * and improved session management capabilities.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final Map<String, String> sessionIdToStaffId = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    
    private final WebSocketSessionManager sessionManager;
    private final WebSocketHeartbeatHandler heartbeatHandler;

    @Override
    public ChannelInterceptor sessionChannelInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String sessionId = accessor.getSessionId();
                StompCommand command = accessor.getCommand();

                if (command == null || sessionId == null) {
                    return message;
                }

                switch (command) {
                    case CONNECT:
                        handleConnect(accessor, sessionId);
                        break;
                    case DISCONNECT:
                        handleDisconnect(sessionId);
                        break;
                    case SEND:
                    case SUBSCRIBE:
                    case UNSUBSCRIBE:
                        // Update last activity time for any interaction
                        updateActivity(sessionId);
                        break;
                    default:
                        break;
                }

                return message;
            }
        };
    }

    private void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        String staffId = getStaffIdFromPrincipal(accessor);
        if (staffId != null) {
            sessionIdToStaffId.put(sessionId, staffId);
            lastActivityMap.put(sessionId, System.currentTimeMillis());
            
            // Register with session manager
            sessionManager.registerSession(sessionId, staffId);
            
            log.info("User {} connected - Session: {}, Total online: {}", 
                    staffId, sessionId, getOnlineStaffIds().size());
        } else {
            // Anonymous connection
            sessionManager.registerSession(sessionId, null);
            log.debug("Anonymous user connected - Session: {}", sessionId);
        }
    }

    private void handleDisconnect(String sessionId) {
        String staffId = sessionIdToStaffId.remove(sessionId);
        lastActivityMap.remove(sessionId);
        
        // Unregister from session manager
        sessionManager.removeSession(sessionId);
        
        if (staffId != null) {
            log.info("User {} disconnected - Session: {}, Total online: {}", 
                    staffId, sessionId, getOnlineStaffIds().size());
        } else {
            log.debug("Anonymous user disconnected - Session: {}", sessionId);
        }
    }

    private void updateActivity(String sessionId) {
        if (sessionId != null) {
            lastActivityMap.put(sessionId, System.currentTimeMillis());
            sessionManager.updateActivity(sessionId);
        }
    }

    private String getStaffIdFromPrincipal(StompHeaderAccessor accessor) {
        var principal = accessor.getUser();
        return principal != null ? principal.getName() : null;
    }

    @Override
    public Set<String> getOnlineStaffIds() {
        // Return only users with healthy sessions (active heartbeats)
        return sessionIdToStaffId.entrySet().stream()
                .filter(entry -> heartbeatHandler.isSessionHealthy(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isOnline(String staffId) {
        if (staffId == null) {
            return false;
        }
        
        // Check if user has at least one healthy session
        return sessionIdToStaffId.entrySet().stream()
                .anyMatch(entry -> staffId.equals(entry.getValue()) && 
                        heartbeatHandler.isSessionHealthy(entry.getKey()));
    }

    @Override
    public long getLastActivityTime(String staffId) {
        if (staffId == null) {
            return 0;
        }
        
        return sessionIdToStaffId.entrySet().stream()
                .filter(entry -> staffId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(lastActivityMap::get)
                .filter(time -> time != null)
                .max(Long::compareTo)
                .orElse(0L);
    }

    @Override
    public Set<String> getSessionsForStaffId(String staffId) {
        if (staffId == null) {
            return Set.of();
        }
        
        return sessionIdToStaffId.entrySet().stream()
                .filter(entry -> staffId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> getStatistics() {
        int totalSessions = sessionIdToStaffId.size();
        int healthySessions = (int) sessionIdToStaffId.keySet().stream()
                .filter(heartbeatHandler::isSessionHealthy)
                .count();
        
        return Map.of(
            "totalSessions", totalSessions,
            "healthySessions", healthySessions,
            "onlineUsers", getOnlineStaffIds().size(),
            "staleSessions", totalSessions - healthySessions
        );
    }

    @Override
    public int cleanupStaleSessions() {
        Set<String> staleSessionIds = sessionIdToStaffId.keySet().stream()
                .filter(sessionId -> !heartbeatHandler.isSessionHealthy(sessionId))
                .collect(Collectors.toSet());
        
        staleSessionIds.forEach(this::handleDisconnect);
        
        if (!staleSessionIds.isEmpty()) {
            log.info("Cleaned up {} stale sessions", staleSessionIds.size());
        }
        
        return staleSessionIds.size();
    }
}
