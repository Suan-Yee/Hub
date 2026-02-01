package com.example.demo.application.usecase;

import org.springframework.messaging.support.ChannelInterceptor;

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
}
