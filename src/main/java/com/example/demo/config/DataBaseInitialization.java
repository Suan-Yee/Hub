package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor @Slf4j
public class DataBaseInitialization implements ApplicationRunner {

    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
       /* User user = User.builder().name("lucas").staffId("99-00002").password(encoder.encode("dat123"))
                .createdAt(LocalDateTime.now()).email("mgsuansyeey@gmail.com").role(Role.ADMIN)
                .build();

        userService.save(user);*/
        User user = userService.findByStaffId("99-00002");
        log.info("User StaffId {}",user);
    }
}
