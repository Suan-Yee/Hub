package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationSseEmitterStore {

    private static final long TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

    private final Map<String, Set<SseEmitter>> emittersByStaffId = new ConcurrentHashMap<>();

    public SseEmitter createAndRegister(String staffId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersByStaffId.computeIfAbsent(staffId, k -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> remove(staffId, emitter));
        emitter.onTimeout(() -> remove(staffId, emitter));
        emitter.onError(e -> remove(staffId, emitter));
        return emitter;
    }

    public void push(String staffId, String message) {
        Set<SseEmitter> emitters = emittersByStaffId.get(staffId);
        if (emitters == null || emitters.isEmpty()) return;
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                log.debug("SSE send failed for staffId={}: {}", staffId, e.getMessage());
                remove(staffId, emitter);
            }
        });
    }

    private void remove(String staffId, SseEmitter emitter) {
        Set<SseEmitter> set = emittersByStaffId.get(staffId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) emittersByStaffId.remove(staffId);
        }
    }
}
