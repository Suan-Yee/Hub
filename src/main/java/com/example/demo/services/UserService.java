package com.example.demo.services;

import com.example.demo.entity.User;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User save(User user);
    User findById(Long userId);
    User findByStaffId(String staffId);
    List<User> findAllUser();
    User findEmailByStaffId(String staffId);
    User findByEmail(String email);
    void saveUserFromExcel(MultipartFile file);
    void updateUsers(MultipartFile file);
    Boolean checkCurrentPassword(String currentPassword);
    ChangePasswordInput changePassword(String currentPassword, String newPassword);
    Boolean isDefaultPassword();
    ChangeDefaultPassword changeDefaultPassword(String newPassword);
    String getUserImageName();
    Page<User> findAllUser(int pageNo, int pageSize);
    Page<User> searchUsers(String query,int pageNo, int pageSize);
}
