package com.example.demo.presentation.rest;

import com.example.demo.config.NotificationSseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationSseEmitterStore sseStore;

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String staffId = principal.getName();
        SseEmitter emitter = sseStore.createAndRegister(staffId);
        return ResponseEntity.ok(emitter);
    }
}
