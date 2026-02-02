package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for WebSocket performance tuning.
 * Allows external configuration via application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "websocket")
@Getter
@Setter
public class WebSocketProperties {

    /**
     * Maximum message size in bytes (default: 128KB).
     */
    private int messageSizeLimit = 128 * 1024;

    /**
     * Send buffer size limit in bytes (default: 1MB).
     */
    private int sendBufferSizeLimit = 1024 * 1024;

    /**
     * Send time limit in milliseconds (default: 20 seconds).
     */
    private int sendTimeLimit = 20 * 1000;

    /**
     * Heartbeat interval in milliseconds (default: 20 seconds).
     */
    private long heartbeatInterval = 20_000;

    /**
     * Maximum missed heartbeats before considering connection stale (default: 3).
     */
    private int maxMissedHeartbeats = 3;

    /**
     * Stale session cleanup interval in milliseconds (default: 5 minutes).
     */
    private long staleSessionCleanupInterval = 5 * 60 * 1000;

    /**
     * Session idle timeout in milliseconds (default: 10 minutes).
     */
    private long sessionIdleTimeout = 10 * 60 * 1000;

    /**
     * Enable compression for WebSocket messages (default: true).
     */
    private boolean compressionEnabled = true;

    /**
     * Thread pool size for message handling (default: 10).
     */
    private int threadPoolSize = 10;

    /**
     * Thread pool queue capacity (default: 200).
     */
    private int threadPoolQueueCapacity = 200;

    /**
     * Enable WebSocket metrics collection (default: true).
     */
    private boolean metricsEnabled = true;

    /**
     * STOMP relay configuration
     */
    private StompRelay relay = new StompRelay();

    /**
     * CORS configuration
     */
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class StompRelay {
        /**
         * Enable STOMP relay (external message broker like RabbitMQ).
         */
        private boolean enabled = false;

        /**
         * Relay host.
         */
        private String host = "localhost";

        /**
         * Relay port.
         */
        private int port = 61613;

        /**
         * Client login.
         */
        private String clientLogin = "guest";

        /**
         * Client passcode.
         */
        private String clientPasscode = "guest";

        /**
         * System login.
         */
        private String systemLogin = "guest";

        /**
         * System passcode.
         */
        private String systemPasscode = "guest";

        /**
         * Virtual host.
         */
        private String virtualHost = "/";
    }

    @Getter
    @Setter
    public static class Cors {
        /**
         * Allowed origins. Cannot use {"*"} when allowCredentials is true (CORS spec).
         * Default empty; set explicitly in application.properties (e.g. http://localhost:3000).
         */
        private String[] allowedOrigins = new String[0];

        /**
         * Allow credentials. Must be false when allowedOrigins is wildcard.
         */
        private boolean allowCredentials = false;

        /**
         * Allowed headers.
         */
        private String[] allowedHeaders = {"*"};

        /**
         * Allowed methods.
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
    }

    /**
     * Validate configuration values.
     */
    public void validate() {
        if (messageSizeLimit <= 0) {
            throw new IllegalArgumentException("messageSizeLimit must be positive");
        }
        if (sendBufferSizeLimit <= 0) {
            throw new IllegalArgumentException("sendBufferSizeLimit must be positive");
        }
        if (sendTimeLimit <= 0) {
            throw new IllegalArgumentException("sendTimeLimit must be positive");
        }
        if (heartbeatInterval <= 0) {
            throw new IllegalArgumentException("heartbeatInterval must be positive");
        }
        if (maxMissedHeartbeats <= 0) {
            throw new IllegalArgumentException("maxMissedHeartbeats must be positive");
        }
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("threadPoolSize must be positive");
        }
        if (threadPoolQueueCapacity < 0) {
            throw new IllegalArgumentException("threadPoolQueueCapacity must be non-negative");
        }
    }
}
