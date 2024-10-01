package com.example.demo.config;


import com.example.demo.enumeration.Role;
import com.example.demo.exception.CustomAccessDeniedHandler;
import com.example.demo.exception.CustomAuthenticationEntryPoint;
import com.example.demo.exception.CustomAuthenticationSuccessHandler;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private String [] URL = {"/login","/error/**","/forgetPassword","/sendCode","/verify/**","/static/**","/resetPassword/**","/resetpassword","/verify-OTPCode","/user/upload","/dashboard"};
    private final UserService userService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.formLogin(form -> form.loginPage("/login")
                .usernameParameter("staffId")
                .loginProcessingUrl("/signIn")
//                .successHandler(new CustomAuthenticationEntryPoint(userService))
                .defaultSuccessUrl("/", true));
        http.logout(logout ->
                logout.invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/"));
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(URL).permitAll()
                        /*.requestMatchers("/user/**").hasAnyAuthority(Role.USER.name())*/
                        .anyRequest().authenticated())
                .exceptionHandling(access -> access.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }
}
