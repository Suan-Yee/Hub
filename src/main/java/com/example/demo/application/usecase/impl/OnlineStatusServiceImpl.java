package com.example.demo.application.usecase.impl;

import com.example.demo.application.usecase.OnlineStatusService;
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

@Service
@Slf4j
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final Map<String, String> sessionIdToStaffId = new ConcurrentHashMap<>();

    @Override
    public ChannelInterceptor sessionChannelInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String sessionId = accessor.getSessionId();

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String staffId = getStaffIdFromPrincipal(accessor);
                    if (staffId != null) {
                        sessionIdToStaffId.put(sessionId, staffId);
                        log.debug("User {} connected, session {}", staffId, sessionId);
                    }
                } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    String staffId = sessionIdToStaffId.remove(sessionId);
                    if (staffId != null) {
                        log.debug("User {} disconnected, session {}", staffId, sessionId);
                    }
                }

                return message;
            }
        };
    }

    private String getStaffIdFromPrincipal(StompHeaderAccessor accessor) {
        var principal = accessor.getUser();
        return principal != null ? principal.getName() : null;
    }

    @Override
    public Set<String> getOnlineStaffIds() {
        return Set.copyOf(sessionIdToStaffId.values());
    }

    @Override
    public boolean isOnline(String staffId) {
        return staffId != null && sessionIdToStaffId.containsValue(staffId);
    }
}
