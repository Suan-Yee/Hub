package com.example.demo.application.usecase;

import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Map;
import java.util.Set;

public interface OnlineStatusService {

    /**
     * Returns the ChannelInterceptor that tracks session connect/disconnect.
     */
    ChannelInterceptor sessionChannelInterceptor();

    /**
     * Returns the staffIds of users with active WebSocket sessions.
     */
    Set<String> getOnlineStaffIds();

    /**
     * Returns true if the user with given staffId has an active WebSocket session.
     */
    boolean isOnline(String staffId);

    /**
     * Get the last activity time for a staff ID (milliseconds since epoch).
     */
    long getLastActivityTime(String staffId);

    /**
     * Get all session IDs for a staff ID.
     */
    Set<String> getSessionsForStaffId(String staffId);

    /**
     * Get online status statistics.
     */
    Map<String, Object> getStatistics();

    /**
     * Clean up stale sessions that are no longer healthy.
     * @return number of sessions cleaned up
     */
    int cleanupStaleSessions();
}
