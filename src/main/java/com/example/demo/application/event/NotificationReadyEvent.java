package com.example.demo.application.event;

/**
 * Emitted after a notification has been persisted and the transaction has committed.
 * Listeners use this to send WebSocket notifications, avoiding race conditions where
 * the client receives the push before the DB has finished saving.
 */
public record NotificationReadyEvent(String recipientStaffId, String message) {}
