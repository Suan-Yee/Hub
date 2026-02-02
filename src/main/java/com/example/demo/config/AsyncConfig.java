package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async processing configuration for non-blocking operations.
 * 
 * Use @Async for:
 * - Email sending
 * - Notification processing
 * - File uploads to cloud
 * - Excel processing
 * - Long-running reports
 * - Logging/analytics
 * 
 * Benefits:
 * - Improves response time
 * - Better resource utilization
 * - Non-blocking operations
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Thread pool for async task execution.
     * 
     * Pool sizing:
     * - Core: 5 threads (always alive)
     * - Max: 20 threads (burst capacity)
     * - Queue: 500 tasks (buffer)
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Pool configuration
        executor.setCorePoolSize(5);           // Min threads
        executor.setMaxPoolSize(20);           // Max threads
        executor.setQueueCapacity(500);        // Queue size
        executor.setKeepAliveSeconds(60);      // Idle thread timeout
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: Caller runs (back pressure)
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        executor.initialize();
        
        log.info("Async task executor initialized - Pool: 5-20 threads, Queue: 500");
        return executor;
    }

    /**
     * Dedicated executor for email sending.
     * Separate pool to avoid blocking other async tasks.
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Email-");
        executor.initialize();
        
        log.info("Email executor initialized - Pool: 2-5 threads");
        return executor;
    }

    /**
     * Dedicated executor for file uploads (Cloudinary).
     * Separate pool for I/O intensive operations.
     */
    @Bean(name = "fileUploadExecutor")
    public Executor fileUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Upload-");
        executor.initialize();
        
        log.info("File upload executor initialized - Pool: 3-10 threads");
        return executor;
    }

    /**
     * Dedicated executor for notification processing.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Notify-");
        executor.initialize();
        
        log.info("Notification executor initialized - Pool: 3-10 threads");
        return executor;
    }

    /**
     * Handle exceptions in async methods.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async method '{}' threw exception: {}", 
                    method.getName(), throwable.getMessage(), throwable);
        };
    }
}
