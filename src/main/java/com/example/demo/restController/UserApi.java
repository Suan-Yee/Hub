package com.example.demo.restController;

import com.example.demo.entity.User;
import com.example.demo.form.ChangeDefaultPassword;
import com.example.demo.form.ChangePasswordInput;
import com.example.demo.services.FileUploadService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor @Slf4j
public class UserApi {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadUsersData(@RequestParam("file")MultipartFile file) throws IOException {

        userService.updateUsers(file);
        return ResponseEntity.ok(Map.of("Message","Users data uploaded"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> listOtherUsers(Principal principal) {
        List<User> users = userService.findAllUser()
                .stream()
                .filter(user -> !user.getStaffId().equals(principal.getName()))
                .collect(Collectors.toList());
        log.info("User list {}",users);
        return ResponseEntity.ok(users);
    }
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
                e.printStackTrace();
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
                e.printStackTrace();
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
}

