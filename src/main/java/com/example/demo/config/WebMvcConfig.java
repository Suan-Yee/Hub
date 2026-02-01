package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/forgetPassword").setViewName("/forgetPassword");
        registry.addViewController("/access-denied").setViewName("/exception/accessDenied");
        registry.addViewController("/bookmark").setViewName("/bookmark");
        registry.addViewController("/pollVoting").setViewName("/poll");
        registry.addViewController("/poll_request").setViewName("/poll_result");
        registry.addViewController("/event").setViewName("/announcement");
        registry.addViewController("/postCreate").setViewName("/postCreateTemplate");
        registry.addViewController("/dashboard").setViewName("/dashboard");
        registry.addViewController("/guidelines").setViewName("/guideLines");
    }
}
