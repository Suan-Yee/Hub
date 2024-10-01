package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor @Slf4j
public class DataBaseInitialization implements ApplicationRunner {

    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        User user = User.builder().name("ivarfdfd").staffId("99-00020").password(encoder.encode("dat123"))
//                .createdAt(LocalDateTime.now()).email("gg@gmail.com").role(Role.ADMIN)
//                .build();
//        userService.save(user);
//        User user = userService.findByStaffId("99-00002");
        /*log.info("User StaffId {}",user);*/
        String staffID = "01-99999";
        User user = userService.findByStaffId(staffID);
        if(user == null){
            User admin = User.builder()
                    .name("Admin")
                    .staffId("01-99999")
                    .password(encoder.encode("Dat123456sysAdmin"))
                    .createdAt(LocalDateTime.now())
                    .email("")
                    .role(Role.SYS_ADMIN)
                    .build();
            userService.save(admin);
            log.info("Create Sys Admin account with {}",staffID);
        }else{
            log.info("Already Exists");
        }
    }
}
