package com.example.demo.presentation.rest;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserInvitationDto;
import com.example.demo.entity.User;
import com.example.demo.exception.SocialGodException;
import com.example.demo.application.usecase.UserInvitationService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserInviteController {

    private final UserService userService;
    private final UserInvitationService  userInvitationService;

@PostMapping("/invitationSend")
public ResponseEntity<Map<String, String>> processedInvitation(@RequestBody UserInvitationDto userInvitationDto) {
    log.info("UserIDs : {}",userInvitationDto.getUserIds());
    if (userInvitationDto.getCommunityId() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Community ID must not be null"));
    }

    try {
        var loginUser = getLoginUser();
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated."));
        }
        userInvitationService.save(loginUser.getId(), userInvitationDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invite successfully!");
        log.info("Invitation sent for community ID: {} to {} users", userInvitationDto.getCommunityId(), userInvitationDto.getUserIds().size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (Exception e) {
        log.error("Failed to process invitation", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
    }
}

    public User getLoginUser() {
        var user = userService.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName());
        return user;
    }


    @GetMapping("/invited-user-display/{id}")
    public ResponseEntity<?> getInvitedUserForDisplay(@PathVariable("id")Long groupId){
       List<UserDto> userDto = userService.usersNotAreInGroup(groupId);

       if(userDto.isEmpty()){
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }else{
           return new ResponseEntity<>(userDto,HttpStatus.OK);
       }
    }

    @GetMapping("/invited-user-display")
    public ResponseEntity<?> getInvitedUserForDisplay(){
        var user = getLoginUser();
        var invitationList = userInvitationService.findLoginUserInvitation(user.getId());
        log.info("InvitationList {}", invitationList);
        if(!invitationList.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(invitationList);
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(Collections.EMPTY_LIST);
        }
    }

    @GetMapping("/accept-invitation/{id}/{communityId}")
    public ResponseEntity<Map<String,String>> acceptInvitation(@PathVariable("id")Long id,@PathVariable("communityId")Long communityId){
        userInvitationService.acceptedInvitation(id,communityId);
        Map<String,String> response = new HashMap<>();
        response.put("message","Accepted Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/decline-invitation/{id}")
    public ResponseEntity<Map<String,String>> declineInvitation(@PathVariable("id")Long id){
        userInvitationService.findById(id);
        Map<String,String> response = new HashMap<>();
        response.put("message","Decline Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/invited-user-count")
    public ResponseEntity<?> getInvitedUserNotiCount(){
        var loginUser = getLoginUser();
        var invitationCount = userInvitationService.findLoginUserInvitation(loginUser.getId());
        if(!invitationCount.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(invitationCount.size());
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(Collections.EMPTY_LIST);
        }
    }


}
