package com.example.demo.application.usecase.impl;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.*;
import com.example.demo.enumeration.Access;
import com.example.demo.enumeration.NotificationType;
import com.example.demo.enumeration.Role;
import com.example.demo.exception.ApiException;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import com.example.demo.form.UserRequestGroupCheck;
import com.example.demo.infrastructure.persistence.repository.GroupRepository;
import com.example.demo.infrastructure.persistence.repository.NotificationRepository;
import com.example.demo.infrastructure.persistence.repository.UserInvitationRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.ExcelUploadService;
import com.example.demo.application.usecase.GroupService;
import com.example.demo.application.usecase.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ExcelUploadService excelUploadService;
    private final BCryptPasswordEncoder encoder;
    private final NotificationRepository notificationRepository;
    private final UserInvitationRepository userInvitationRepository;
    private final GroupRepository groupRepository;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    @Transactional(readOnly = true)
    public List<UserDto> findAllUser() {
        List<User> userList = userRepository.findAll();
        List<UserDto> userDto = userList.stream().map(UserDto::new).collect(Collectors.toList());
        return userDto;

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
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
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
    @Transactional(readOnly = true)
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
        String sysAdmin = "01-99999";
        if (excelUploadService.isExcelValid(file)) {
            try {
                List<User> usersFromDB = userRepository.findAll();
                List<User> usersFromExcel = excelUploadService.getUserData(file.getInputStream());

                for (User userFromDB : usersFromDB) {
                    if(sysAdmin.equals(userFromDB.getStaffId())){
                        continue;
                    }
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
    @Override
    public List<User> findByAccess() {
        return userRepository.findAllByAccess(Access.ONLINE);
    }


    @Override
    public List<User> findByStatus(boolean b) {
        return userRepository.findByStatus(b);
    }

    @Override
    public List<UserDto> findByNameContaining(String name) {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> userList = userRepository.findByNameContainingAndStaffIdNot(name,staffId);
        return userList.stream().map(UserDto::new).toList();
    }

    @Override
    public User findByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public List<UserDto> usersNotAreInGroup(Long groupId) {
        List<User> userList = userRepository.findUsersNotInGroup(groupId);
        return userList.stream().map(UserDto::new).toList();
    }

    @Override
    public List<String> fetchGroupUserProfile(Long groupId) {
        List<User> userList = userRepository.getAllUserFromGroup(groupId);
        return userList.stream().map(User::getPhoto).limit(5).toList();
    }

    @Override
    @Transactional
    public User updateRolewithNoti(Long id, String role,Long triggeredByUserId) {
        User user = userRepository.findById(id).orElse(null);
        User triggeredByUser = userRepository.findById(triggeredByUserId).orElseThrow(() -> new RuntimeException("Triggered by user not found"));
       /* user.setRole(Role.valueOf(role));*/
        userRepository.save(user);

        NotificationType type = null;

        String messageType = "USER";
        if ("ADMIN".equals(role)) {
            messageType = "access_granted";
            type = NotificationType.ACCESS_GRANTED;
        } else if ("USER".equals(role)) {
            messageType = "access_denied";
            type = NotificationType.ACCESS_DENIED;
        }

        List<Notification> existingNotifications= notificationRepository.findByRecipientId(id);
        if (!existingNotifications.isEmpty()) {
            for (Notification existingNotification : existingNotifications) {
                // Update existing notification
                existingNotification.setTriggeredBy(triggeredByUser);
                existingNotification.setTime(LocalDateTime.now());
                existingNotification.setStatus(false);
                existingNotification.setType(type);
                existingNotification.setMessage(messageType);
                notificationRepository.save(existingNotification);
            }
        } else {
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTriggeredBy(triggeredByUser);
            notification.setMessage(messageType);
            notification.setTime(LocalDateTime.now());
            notification.setStatus(false);
            notification.setType(type);
            notificationRepository.save(notification);
        }

        return user;
    }
    public UserRequestGroupCheck checkUserRequest(Principal principal, Long groupId) {
        String staffId = principal.getName();
        User user = findByStaffId(staffId);

        // Check if user is in the group
        boolean isUserInGroup = userRepository.findUserInGroup(user.getId(), groupId).isPresent();
        Group group = groupRepository.findById(groupId).orElse(null);
        UserRequestGroupCheck check = new UserRequestGroupCheck(user);

        if (isUserInGroup) {
            check.setStatus("joined");
            if(user.getId().equals(group.getGroupOwner().getId())){
                check.setAdmin(true);
            }
        } else {
            boolean hasRequestedToJoin = userInvitationRepository.getInfoForUserRequest(user.getId(), groupId).isPresent();
            if (hasRequestedToJoin) {
                check.setStatus("requested");
            } else {
                check.setStatus("visitor");
            }
        }

        return check;
    }

    @Override
    public void turnToggleNoti(Long userId) {
        User user = findById(userId);
        user.setTurnNoti(!user.isTurnNoti());
        userRepository.save(user);
    }

//    public boolean isUserBirthday(String username, LocalDate date) {
//        return userRepository.findByStaffId(username)
//                .map(user -> LocalDate.parse(user.getDob(), dateFormatter).equals(date))
//                .orElse(false);
//    }
//
//    public int getUserAge(String username) {
//        return userRepository.findByStaffId(username)
//                .map(user -> Period.between(LocalDate.parse(user.getDob(), dateFormatter), LocalDate.now()).getYears())
//                .orElse(0);
//    }
}
