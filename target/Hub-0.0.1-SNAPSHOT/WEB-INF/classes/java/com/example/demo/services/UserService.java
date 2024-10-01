package com.example.demo.services;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import com.example.demo.form.UserRequestGroupCheck;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

public interface UserService {

    User save(User user);
    User findById(Long userId);
    User findByStaffId(String staffId);
    List<UserDto> findAllUser();
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
    User findAuthenticatedUser(Principal principal);
    public List<User> findByAccess();
    public List<User> findByStatus(boolean b);

    List<UserDto> findByNameContaining(String name);

    User findByName(String name);
    List<UserDto> usersNotAreInGroup(Long groupId);
    List<String> fetchGroupUserProfile(Long groupId);

    User updateRolewithNoti(Long id, String role, Long triggeredByUserId);
    UserRequestGroupCheck checkUserRequest(Principal principal, Long groupId);
//    boolean isUserBirthday(String username, LocalDate date);
//    int getUserAge(String username);

    void turnToggleNoti(Long userId);

}
