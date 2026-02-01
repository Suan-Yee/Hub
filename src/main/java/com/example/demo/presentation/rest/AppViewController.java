package com.example.demo.presentation.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class AppViewController implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/logout").setViewName("/login");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/forgetPassword").setViewName("/forgetPassword");
        registry.addViewController("/access-denied").setViewName("/exception/accessDenied");
       /* registry.addViewController("/profile").setViewName("/admin_profile");*/
        registry.addViewController("/userList").setViewName("/userList");
        registry.addViewController("/bookmark").setViewName("/bookmark");
        registry.addViewController("/pollVoting").setViewName("/poll");
        registry.addViewController("/poll_request").setViewName("/poll_result");
        registry.addViewController("/event").setViewName("/announcement");
        registry.addViewController("/postCreate").setViewName("/postCreateTemplate");
        registry.addViewController("/dashboard").setViewName("/dashboard");
        registry.addViewController("/guidelines").setViewName("/guideLines");
    }
}
