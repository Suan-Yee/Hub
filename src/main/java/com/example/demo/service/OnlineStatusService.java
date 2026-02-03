package com.example.demo.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;
import java.util.Set;

public interface OnlineStatusService {

    /**
     * Handle user connection
     */
    void handleConnect(String sessionId, String username);

    /**
     * Handle user disconnection
     */
    void handleDisconnect(String sessionId);

    /**
     * Get all online users
     */
    Set<String> getOnlineUsers();

    /**
     * Check if a user is online
     */
    boolean isUserOnline(String username);

    /**
     * Get online user count
     */
    int getOnlineUserCount();

    /**
     * Intercept messages for heartbeat and connection tracking
     */
    Message<?> preSend(Message<?> message, MessageChannel channel);

    /**
     * Process heartbeat from client
     */
    void processHeartbeat(String sessionId);

    /**
     * Clean up stale sessions
     */
    void cleanupStaleSessions();

    /**
     * Get all session IDs for a username
     */
    List<String> getSessionsForUser(String username);
}
