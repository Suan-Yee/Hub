package com.example.demo.services.impl;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.exception.ApiException;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.ExcelUploadService;
import com.example.demo.services.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ExcelUploadService excelUploadService;
    private final BCryptPasswordEncoder encoder;

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

    @Override
    public Boolean checkCurrentPassword(String currentPassword){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        if (user!=null){
            log.info("User Password: {}",user.getPassword());
            return encoder.matches(currentPassword,user.getPassword());
        }
        else return false;
    }
    @Override
    public ChangePasswordInput changePassword(String currentPassword, String newPassword){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        if(user!=null){
            if(encoder.matches(currentPassword,user.getPassword())){
                user.setPassword(encoder.encode(newPassword));
                userRepository.save(user);
            }
            return new ChangePasswordInput();
        }
        return new ChangePasswordInput();
    }
    @Override
    public Boolean isDefaultPassword(){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        if(user!=null){
            if(encoder.matches("dat123",user.getPassword())){
                return true;
            }
        }
        return false;

    }
    @Override
    public ChangeDefaultPassword changeDefaultPassword(String newPassword){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        log.info("StaffId : {}",user.getName());
        if(user!=null){
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        }
        return new ChangeDefaultPassword();
    }
    @Override
    public String getUserImageName(){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        if(user.getPhoto()!=null){
            return user.getPhoto();
        }
        else return null;
    }

    @Override
    public Page<User> findAllUser(int pageNo, int pageSize){
        Pageable pageable= PageRequest.of(pageNo-1,pageSize);
        return userRepository.findAll(pageable);
    }
    @Override
    public Page<User> searchUsers(String query, int pageNo, int pageSize) {
        Specification<User> spec = (Root<User> root, CriteriaQuery<?> search, CriteriaBuilder builder) -> {
            if (query == null || query.isEmpty()) {
                return builder.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            if (query.contains("@")) {
                predicates.add(builder.like(root.get("email"), "%" + query + "%"));
            } else {
                predicates.add(builder.like(root.get("name"), "%" + query + "%"));
                predicates.add(builder.like(root.get("staffId"), "%" + query + "%"));
            }
            return builder.or(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public User findAuthenticatedUser(Principal principal) {
        User currentUser = findByStaffId(principal.getName());
        User user = findById(currentUser.getId());
        return user;
    }

    @Override
    public void saveUserFromExcel(MultipartFile file) {
        if(excelUploadService.isExcelValid(file)){
            try{
                List<User> userList = excelUploadService.getUserData(file.getInputStream());
                userList.forEach(user -> user.setPassword(encoder.encode("Dat@123.com")));
                userRepository.saveAll(userList);
            }catch (IOException e){
                throw new ApiException(e.getMessage());
            }
        }
    }

    @Override
    public void updateUsers(MultipartFile file) {
        if (excelUploadService.isExcelValid(file)) {
            try {
                List<User> usersFromDB = userRepository.findAll();
                List<User> usersFromExcel = excelUploadService.getUserData(file.getInputStream());

                for (User userFromDB : usersFromDB) {
                    boolean found = false;
                    for (User userFromExcel : usersFromExcel) {
                        if (userFromDB.getStaffId().equals(userFromExcel.getStaffId())) {
                            userFromDB.setName(userFromExcel.getName());
                            userFromDB.setDoor_log_number(userFromExcel.getDoor_log_number());
                            userFromDB.setDivision(userFromExcel.getDivision());
                            userFromDB.setTeam(userFromExcel.getTeam());
                            userFromDB.setDepartment(userFromExcel.getDepartment());
                            userRepository.save(userFromDB);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        userFromDB.setStatus(false);
                        userRepository.save(userFromDB);
                    }
                }
                for (User userFromExcel : usersFromExcel) {
                    boolean found = false;
                    for (User userFromDB : usersFromDB) {
                        if (userFromDB.getStaffId().equals(userFromExcel.getStaffId())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        userFromExcel.setPassword(encoder.encode("dat123"));
                        userFromExcel.setRole(Role.USER);
                        userFromExcel.setStatus(true);
                        userRepository.save(userFromExcel);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
