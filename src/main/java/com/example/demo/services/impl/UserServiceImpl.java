package com.example.demo.services.impl;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByStaffId(String staffId) {
        return userRepository.findByStaffId(staffId).orElse(null);
    }

    @Override
    public List<User> findAllUser() {
        List<User> userList = userRepository.findAll();
        return userList;
    }

    @Override
    public User findEmailByStaffId(String staffId) {
        return userRepository.findEmailByStaffId(staffId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String staffId) throws UsernameNotFoundException {
        User user = userRepository.findByStaffId(staffId).orElse(null);
        log.info("User info from load {}",user);
        return new AppUser(user);
    }
}
