package com.example.demo.config;


//import com.example.demo.exception.CustomAccessDeniedHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//
//@EnableWebSecurity
//@EnableMethodSecurity
//@Configuration
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private static final String[] PUBLIC_URLS = {
//            "/error/**",
//            "/sendCode",
//            "/verify/**",
//            "/resetpassword",
//            "/api/auth/login",
//            "/api/posts/**"
//    };
//    private static final String[] ADMIN_ONLY_URLS = {
//            "/api/websocket/**"
//    };
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        // TEMPORARY: Security disabled for testing
//        http.csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()); // Allow all requests without authentication
//
//        // COMMENTED OUT: Original security configuration
//        // http.csrf(AbstractHttpConfigurer::disable)
//        //         .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//        //         .authorizeHttpRequests(auth -> auth
//        //                 .requestMatchers(PUBLIC_URLS).permitAll()
//        //                 .requestMatchers(ADMIN_ONLY_URLS).hasRole("ADMIN")
//        //                 .anyRequest().authenticated())
//        //         .exceptionHandling(access -> access
//        //                 .accessDeniedHandler(new CustomAccessDeniedHandler())
//        //                 .authenticationEntryPoint(this::unauthorizedResponse));
//
//        return http.build();
//    }
//
//    @Bean
//    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }
//
//    private void unauthorizedResponse(jakarta.servlet.http.HttpServletRequest request,
//                                      jakarta.servlet.http.HttpServletResponse response,
//                                      AuthenticationException exception) throws java.io.IOException {
//        response.setStatus(401);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
//    }
//}
