package com.example.demo.restController;


import com.example.demo.dto.GroupDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.User;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;
    private final UserHasGroupService userHasGroupService;
    private final UserService userService;
    private final  FileUploadService fileUploadService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        log.info("All User : {}",userService.findAllUser());

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUser());
    }

    @PostMapping("/createCommunity")
    public ResponseEntity<Map<String, String>> createGroup(@ModelAttribute GroupDto group,
                                                           @RequestParam("file") MultipartFile file, Principal principal) throws IOException {

        if(file !=null && file.getSize()>0){
            String groupImageUrl = fileUploadService.uploadGroupImage(file);
            group.setImage(groupImageUrl);
        }
        log.info("File {}",file.getOriginalFilename());
        groupService.createCommunity(group,principal);
        Map<String,String> response = new HashMap<>();
        response.put("message","Created successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/communityview")
    @ResponseBody
    public List<GroupDto> view() {
        List<GroupDto> communities = groupService.findAllGroup();
        log.info("Communites List",communities);
        return communities;
    }

    @PostMapping("/createGroup")
    public ResponseEntity<Map<String, String>> createCommunity(@ModelAttribute GroupDto group, @RequestParam("user") Long[] user) {
        System.out.println(user.length);
        groupService.createGroup(group, Arrays.asList(user));
        Map<String,String> response = new HashMap<>();
        response.put("message","Created successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/ownerNames/{communityId}")
    public ResponseEntity<?> getUserNames(@PathVariable Long communityId) {
        try {
            List<String> ownerNames = groupService.getOwnerNamesByCommunityId(communityId);
            return ResponseEntity.ok(ownerNames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching owner names");
        }
    }

    @DeleteMapping("/delete/{communityId}")
    public ResponseEntity<?> deleteGroup(@PathVariable("communityId") Long communityId) {
        Optional<Group> optionalGroup = groupService.findById(communityId);
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setDeleted(true);
            groupService.save(group);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{communityId}")
    public ResponseEntity<List<User>> getAllUsersByCommunity(@PathVariable("communityId")Long communityId,Model model){
        var community = groupService.getCommunityBy(communityId);
        List<UserHasGroup> userHasGroups = userHasGroupService.findByGroupId(communityId);
        List<User> users = new ArrayList<>();
        for(UserHasGroup userHasGroup:userHasGroups){
            User user = userService.findById(userHasGroup.getUser().getId());
            users.add(user);
        }
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/loginUserGroups")
    @ResponseBody
    public ResponseEntity<List<Group>> findAllGroupOfCurrentUser(){
        return ResponseEntity.status(HttpStatus.OK).body(groupService.getAllCommunityWithUserId());
    }

//    @PostMapping("/kick")
//    public ResponseEntity<Map<String, String>> kickCommunity(@ModelAttribute Group group, @RequestParam("userIds") Long[] user) {
//        groupService.kickGroup(group, Arrays.asList(user));
//        Map<String,String> response = new HashMap<>();
//        response.put("message","Kicked successfully");
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    @PostMapping("/kick")
    public ResponseEntity<Map<String, String>> kickCommunity(@ModelAttribute Group group, @RequestParam("userIds") Long[] userIds) {
        // Perform authentication and authorization checks here if not handled elsewhere
        try {
            groupService.kickGroup(group, Arrays.asList(userIds));
            Map<String,String> response = new HashMap<>();
            response.put("message","Users kicked successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Log the error for debugging and return an appropriate error message
            Map<String,String> response = new HashMap<>();
            response.put("message","Error kicking users from the community");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("getCommunity/{communityId}")
    public ResponseEntity<Group> getCommunityById(@PathVariable Long communityId) {
        Group group = groupService.getCommunityById(communityId);
        if (group != null) {
            return ResponseEntity.ok(group);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
