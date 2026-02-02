package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Query performance monitoring using AOP.
 * Logs slow queries and provides performance insights.
 */
@Aspect
@Component
@Slf4j
public class QueryPerformanceConfig {

    private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 second

    /**
     * Monitor repository method execution time.
     */
    @Around("execution(* com.example.demo.infrastructure.persistence.repository..*(..))")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            
            if (executionTime > SLOW_QUERY_THRESHOLD_MS) {
                log.warn("SLOW QUERY detected: {} took {}ms", methodName, executionTime);
            } else if (executionTime > 500) {
                log.debug("Query: {} took {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("Query FAILED: {} after {}ms - {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor service layer performance.
     */
    @Around("execution(* com.example.demo.application.usecase.impl..*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            
            if (executionTime > 2000) {
                log.warn("SLOW SERVICE: {} took {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("Service FAILED: {} after {}ms", methodName, executionTime);
            throw e;
        }
    }
}
