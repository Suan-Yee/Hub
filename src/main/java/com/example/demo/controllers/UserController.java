package com.example.demo.controllers;

import com.example.demo.entity.User;
import com.example.demo.services.ExcelUploadService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadUsersData(@RequestParam("file")MultipartFile file) throws IOException {

        userService.updateUsers(file);
        return ResponseEntity.ok(Map.of("Message","Users data uploaded"));
    }

    @GetMapping("/userList")
    public ResponseEntity<?> userList(){
        List<User> users = userService.findAllUser();
        return ResponseEntity.ok(users);
    }
}

