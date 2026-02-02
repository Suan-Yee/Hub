package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Overall application performance configuration.
 * 
 * Features:
 * - Response compression (gzip)
 * - HTTP caching headers
 * - ETag support
 * - Tomcat optimization
 */
@Configuration
@Slf4j
public class PerformanceConfig implements WebMvcConfigurer {

    /**
     * Enable shallow ETag generation for conditional requests.
     * ETags help reduce bandwidth by allowing clients to cache responses.
     */
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        log.info("ETag filter enabled for conditional HTTP requests");
        return new ShallowEtagHeaderFilter();
    }

    /**
     * Configure HTTP caching for static resources.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cache static resources for 1 hour
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1))
                        .cachePublic()
                        .mustRevalidate());
        
        log.debug("HTTP caching configured for static resources (1 hour)");
    }

    /**
     * Tomcat server optimization.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            // Connection settings
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("maxThreads", "200");
                connector.setProperty("minSpareThreads", "20");
                connector.setProperty("maxConnections", "10000");
                connector.setProperty("acceptCount", "100");
                connector.setProperty("connectionTimeout", "20000");
                connector.setProperty("keepAliveTimeout", "60000");
                connector.setProperty("maxKeepAliveRequests", "100");
                
                // Enable compression
                connector.setProperty("compression", "on");
                connector.setProperty("compressionMinSize", "1024");
                connector.setProperty("compressibleMimeType", 
                        "text/html,text/xml,text/plain,text/css,text/javascript," +
                        "application/javascript,application/json,application/xml");
            });
            
            log.info("Tomcat optimized - Max threads: 200, Compression: ON, Max connections: 10000");
        };
    }
}
