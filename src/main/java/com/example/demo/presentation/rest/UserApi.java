package com.example.demo.presentation.rest;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.GuideLines;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import com.example.demo.form.UserRequestGroupCheck;
import com.example.demo.application.usecase.FileUploadService;
import com.example.demo.application.usecase.GuideLinesService;
import com.example.demo.application.usecase.NotificationService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor @Slf4j
public class UserApi {

    private final UserService userService;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;
    private final GuideLinesService guideLinesService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadUsersData(@RequestParam("file")MultipartFile file) throws IOException {

        userService.updateUsers(file);
        return ResponseEntity.ok(Map.of("Message","Users data uploaded"));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(Principal principal){
       User user = userService.findAuthenticatedUser(principal);
        UserDto userDto = new UserDto(user);
       return new ResponseEntity<>(userDto,HttpStatus.OK);
    }

    @GetMapping("/checkUserRequest/{id}")
    public ResponseEntity<?> checkUserRequest(Principal principal,@PathVariable("id") Long groupId){
        UserRequestGroupCheck check = userService.checkUserRequest(principal,groupId);
        if(check != null){
            return new ResponseEntity<>(check,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @GetMapping("/all")
//    public ResponseEntity<?> listOtherUsers(Principal principal) {
//        List<User> users = userService.findAllUser()
//                .stream()
//                .filter(user -> !user.getStaffId().equals(principal.getName()))
//                .collect(Collectors.toList());
//        log.info("User list {}",users);
//        return ResponseEntity.ok(users);
//    }
    //methods from whp
    @PostMapping("/change-default-password")
    public ResponseEntity<ChangeDefaultPassword> changeDefaultPassword(@RequestBody ChangeDefaultPassword changeDefaultPassword){
        ChangeDefaultPassword chdpw= userService.changeDefaultPassword(changeDefaultPassword.getNewPassword());
        if(chdpw!=null){
            chdpw.setNewPassword(changeDefaultPassword.getNewPassword());
            return ResponseEntity.ok(chdpw);
        }
        else return ResponseEntity.notFound().build();
    }
    @GetMapping("/check-default-password")
    public Boolean isDefaultPassword(){
        return userService.isDefaultPassword();
    }
    @PostMapping("/check-current-password")
    public Boolean checkCurrentPassword(@RequestBody ChangePasswordInput changePasswordInput){
        log.info("ChangePasswordInput {}",changePasswordInput.getCurrentPassword());
        return userService.checkCurrentPassword(changePasswordInput.getCurrentPassword());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordInput> changePassword(@RequestBody ChangePasswordInput changePasswordInput){
        ChangePasswordInput chpwd = userService.changePassword(changePasswordInput.getCurrentPassword(),changePasswordInput.getNewPassword());
        if(chpwd!=null){
            chpwd.setCurrentPassword(changePasswordInput.getCurrentPassword());
            chpwd.setNewPassword(changePasswordInput.getNewPassword());
            changePasswordInput.setComfirmPassword(changePasswordInput.getComfirmPassword());
            return ResponseEntity.ok(chpwd);
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/get-user-image")
    public ResponseEntity<String> getUserImageName(){
        String imageUrl = userService.getUserImageName();
        log.info("User Image :{}",imageUrl);
        return ResponseEntity.ok(imageUrl);
    }
    @GetMapping("/getImageFromProfile")
    public ResponseEntity<String> getUserImageNameFromHeader(){
        String imageUrl = userService.getUserImageName();
        log.info("User Image :{}",imageUrl);
        return ResponseEntity.ok(imageUrl);
    }
    @PostMapping("remove-profile-image")
    public ResponseEntity<User> removeProfileImage(){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(user!=null){
            try {
                fileUploadService.deleteImage(user.getPhoto());
            } catch (IOException e) {
                log.error("Failed to delete profile image", e);
            }
        }
        user.setPhoto(null);
        userService.save(user);
        log.info("Delete Image {}");
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        else return ResponseEntity.notFound().build();
    }
    @PostMapping("/save-new-image")
    public ResponseEntity<User> saveNewImage(@RequestParam("file") MultipartFile image){
        log.info("Image File :{}",image.getName());
        String imageUrl=null;
        if(image!=null && image.getSize()>0){
            try {
                imageUrl = fileUploadService.uploadImage(image);
            } catch (IOException e) {
                log.error("Failed to upload profile image", e);
            }
        }
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        log.info("Image Url {}",imageUrl);
        user.setPhoto(imageUrl);
        userService.save(user);
        if(user!=null){
            log.info("Successful save");
            return ResponseEntity.ok(user);
        }
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<?> mentionUser(@RequestParam String query){
        List<UserDto> userDtoList = userService.findByNameContaining(query);

        return ResponseEntity.ok(userDtoList);
    }

    @GetMapping("/groupUserProfile/{id}")
    public ResponseEntity<?> getAllUserProfile(@PathVariable("id") Long groupId){
        List<String> profileList= userService.fetchGroupUserProfile(groupId);
        if(profileList != null && !profileList.isEmpty()){
            return new ResponseEntity<>(profileList,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/userprofile")
    public ResponseEntity<?> getImgandName(Principal principal){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(user==null){
            return ResponseEntity.notFound().build();
        }else
            return ResponseEntity.ok(user);
    }

    /*for change user Role by hpy*/
    @PostMapping("/updateUserRole")
    public ResponseEntity<?> changeRole(Principal principal,@RequestParam("userId") Long userId,@RequestParam String role){
        log.info("userId222: {}", userId);
        User triggeredByUser= userService.findByStaffId(principal.getName());
        Long triggeredByUserId= triggeredByUser.getId();
        User user= userService.findById(userId);
        user.setTriggeredByUserId(triggeredByUserId);
        if(role.equals("ADMIN")){
            user.setRoleChangePending(true);
            userService.updateRolewithNoti(userId, role, triggeredByUserId);
        }else{
            user.setRole(Role.valueOf(role));
            userService.updateRolewithNoti(userId, role, triggeredByUserId);
        }
        /*user.setRole(Role.valueOf(role));*/
        userService.save(user);
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping("/changeAdmin")
    public ResponseEntity<?> changeintoAdmin(Principal principal){
        User user= userService.findByStaffId(principal.getName());
        user.setRole(Role.valueOf("ADMIN"));
        user.setRoleChangePending(false);
        userService.save(user);
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping("/changeUser")
    public ResponseEntity<?> changeintoUser(Principal principal){
        User user= userService.findByStaffId(principal.getName());
        user.setRole(Role.valueOf("USER"));
        user.setRoleChangePending(false);
        userService.save(user);
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        else
            return ResponseEntity.notFound().build();
    }

    /*get noti for changing role to user*/
    @GetMapping("/notification")
    public ResponseEntity<?> getnoti(Principal principal){
        User user=userService.findByStaffId(principal.getName());
        Long userId=user.getId();
        log.info("userId for noti:{}",userId);
        List<Notification> noti= notificationService.findByUserId(userId);
        log.info("Notificaion for role:{}",noti);
        if(noti ==null){
            return ResponseEntity.notFound().build();
        }else
            return ResponseEntity.ok(noti);
    }

    /*get modal box for changing role to admin*/
    @GetMapping("/getrolepending")
    public ResponseEntity<?> rolechange(Principal principal){
        User user = userService.findByStaffId(principal.getName());
        if (user.isRoleChangePending()) {
            return ResponseEntity.ok(true); // Role change is pending
        } else
            return ResponseEntity.ok(false); // Role change is not pending
    }


    /*for create policy by hpy*/
    @PostMapping("/savePolicy")
    public ResponseEntity<?> createGuideLines(Principal principal, @RequestBody GuideLines guideLines) {
        log.info("Received request to create guidelines");
        User user = userService.findByStaffId(principal.getName());
        if (user == null) {
            log.error("User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        guideLines.setUser(user);
        GuideLines savedGuidelines = guideLinesService.save(guideLines);
        if (savedGuidelines == null) {
            log.error("Failed to save guidelines");
            return ResponseEntity.notFound().build();
        }
        log.info("Guidelines created successfully");
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/getPolicy")
    public ResponseEntity<?> getPolicytoEdit(){
        List <GuideLines> guideLines= guideLinesService.findAll();
        if(guideLines!=null){
            return ResponseEntity.ok(guideLines);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/updatePolicy")
    public ResponseEntity<?> updatePolicy(Principal principal,@RequestBody GuideLines guideLines){
        User user = userService.findByStaffId(principal.getName());
        GuideLines data = guideLinesService.findById(guideLines.getId()).orElse(null);
        if(data == null){
            return ResponseEntity.notFound().build();
        }
        log.info("Policy message: {}",guideLines.getMessage());
        log.info("Policy Id: {}",guideLines.getId());
        data.setMessage(guideLines.getMessage());
        data.setUser(user);
        guideLinesService.save(data);
        return ResponseEntity.ok(guideLines);
    }

    /*create dob and bio*/
    @PostMapping("/createUserDetail")
    public ResponseEntity<?> createUser(Principal principal,@RequestBody UserDto userDto){
        User user = userService.findByStaffId(principal.getName());
        user.setBiography(userDto.getBiography());
        user.setDob(userDto.getDob());
        userService.save(user);
        if(user!=null){
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/check-birthday")
//    public Map<String, Object> checkBirthday(Principal principal) {
//        String staffId = principal.getName();
//        LocalDate today = LocalDate.now();
//        Map<String, Object> response = new HashMap<>();
//        response.put("isBirthday", userService.isUserBirthday(staffId, today));
//        response.put("age", userService.getUserAge(staffId));
//        return response;
//    }
}

