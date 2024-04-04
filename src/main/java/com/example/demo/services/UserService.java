package com.example.demo.services;

import com.example.demo.entity.User;

import java.util.List;

public interface UserService {

    User save(User user);
    User findById(Long userId);
    User findByStaffId(String staffId);
    List<User> findAllUser();
    User findEmailByStaffId(String staffId);
    User findByEmail(String email);
}
