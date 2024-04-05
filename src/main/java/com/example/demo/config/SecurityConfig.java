package com.example.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private String [] URL = {"/login","forgetPassword","/sendCode","/verify/**","/static/**","/resetPassword/**","/resetpassword","/verify-OTPCode","/user/upload","/dashboard"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.formLogin(form -> form.loginPage("/login").usernameParameter("staffId").loginProcessingUrl("/signIn").defaultSuccessUrl("/"));
        http.logout(logout ->  logout.invalidateHttpSession(true).clearAuthentication(true).logoutUrl("/logout").logoutSuccessUrl("/"));

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(URL).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
