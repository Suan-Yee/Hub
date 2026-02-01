package com.example.demo.presentation.rest;


import com.example.demo.dto.*;
import com.example.demo.entity.Group;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.User;
import com.example.demo.form.GroupForm;
import com.example.demo.form.GroupUserDisplay;
import com.example.demo.form.UserKickForm;
import com.example.demo.application.usecase.*;
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
    private final UserInvitationService userInvitationService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        log.info("All User : {}",userService.findAllUser());
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUser());
    }

    @PostMapping("/createCommunity")
    public Boolean createGroup(@ModelAttribute GroupDto group,
                                                           @RequestParam("file") MultipartFile file,
                                                           Principal principal) throws IOException {
        log.info("Visibility {}",group.getVisibility());
        if ("1".equals(group.getVisibility())) {
            log.info("Visibility is 1");
            group = group.withPrivate(true);
        } else {
            group = group.withPrivate(false);
        }

        log.info("File {}",file);

        if (file != null && file.getSize() > 0) {
            String groupImageUrl = fileUploadService.uploadGroupImage(file);
            group = group.withImage(groupImageUrl);
        }
        groupService.createCommunity(group, principal);

       /* Map<String, String> response = new HashMap<>();
        response.put("message", "Created successfully");*/

        return true;
    }

    @GetMapping("/communityview")
    @ResponseBody
    public List<GroupDto> view() {
        List<GroupDto> communities = groupService.findGroupThatUserIsIn();
        log.info("Communites List",communities);
        return communities;
    }

    @GetMapping("/inactive-community-view")
    @ResponseBody
    public ResponseEntity<List<GroupDto>> getInactiveCommunity() {
        var user = userService.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName());
        List<GroupDto> inactiveCommunities = groupService.findAllInactiveGroup();
        log.info("Inactive Group {}",inactiveCommunities);
        return ResponseEntity.status(HttpStatus.OK).body(inactiveCommunities);
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
    public ResponseEntity<?> getAllUsersByCommunity(@PathVariable("communityId")Long communityId,Model model){

        List<UserDto> userList = userHasGroupService.findByGroupId(communityId);
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }

    @GetMapping("/loginUserGroups")
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
    public ResponseEntity<?> getCommunityById(@PathVariable Long communityId) {
        GroupDto group = groupService.getCommunityById(communityId);
        if (group != null) {
            return ResponseEntity.ok(group);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
//    @PostMapping("")
//    public ResponseEntity<?> kickUser(@RequestBody UserKickForm userKickForm){
//        groupService.kickUser(userKickForm.getUserId(),userKickForm.getGroupId());
//    }

    //sya
    @GetMapping("/groupForm")
    public ResponseEntity<?> getAllGroupForm(Principal principal){
        List<GroupForm> groupFormList = groupService.getAllGroupForm(principal);
        if(groupFormList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(groupFormList,HttpStatus.OK);
        }
    }
    //sya
    @GetMapping("/randomGroup")
    public ResponseEntity<?> getRandomGroup(){
        List<GroupForm> groupDtoList = groupService.randomGroupUserNotIn();

        if(groupDtoList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(groupDtoList,HttpStatus.OK);
        }
    }
    //sya
    @GetMapping("/groupDetails/{id}")
    public ResponseEntity<?> getGroupDetails(@PathVariable("id")Long groupId){
        GroupDto groupDto = groupService.findGroupsByIds(groupId);

        if(groupDto == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(groupDto,HttpStatus.OK);
        }
    }
    @GetMapping("/get-posts-in-each-group")
    public ResponseEntity<GroupReport> getPostsInEachGroup(){
        GroupReport groupReports = groupService.findTop5ByPostCount();
        log.info("Group Reports {}",groupReports);
        if(groupReports!=null){
            return ResponseEntity.ok(groupReports);
        }
        return (ResponseEntity<GroupReport>) ResponseEntity.status(404);
    }

    @GetMapping("/get-members-in-each-group")
    public ResponseEntity<GroupReport> getMembersInEachGroup(){
        GroupReport groupReports = groupService.findTop5ByMemberCount();
        if(groupReports!=null){
            return ResponseEntity.ok(groupReports);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/get-post-population-in-trending-group")
    public ResponseEntity<GroupPostPopulation> getPostsPopulationInGroup(){
        GroupPostPopulation groupPostPopulation =groupService.getPostPopulationByGroup(1L,2024);
        if(groupPostPopulation!=null){
            return ResponseEntity.ok(groupPostPopulation);
        }else return ResponseEntity.notFound().build();
    }
    @GetMapping("/get-group-population-by-year-and-groupId")
    public ResponseEntity<GroupPostPopulation> getGroupPopulationByYearAndGroupId( @RequestHeader("Year") int year,
                                                                                   @RequestHeader("GroupId") Long groupId){

        GroupPostPopulation groupPostPopulation =groupService.getPostPopulationByGroup(groupId,year);
        if(groupPostPopulation!=null){
            return ResponseEntity.ok(groupPostPopulation);
        }else return ResponseEntity.notFound().build();
    }

    //sya
    @PostMapping("/group/requestToJoin")
    public ResponseEntity<?> userRequestToJoin(@RequestBody GroupDto groupDto){
        Long groupId = groupDto.getId();
        log.info("GroupId is {}",groupId);
        log.info("Method Reached");
        boolean result = groupService.userRequestToGroup(groupId);
        if(result){
            return new ResponseEntity<>(HttpStatus.OK);
        }else return ResponseEntity.notFound().build();
    }

    @GetMapping("/group/userList/{id}")
    public ResponseEntity<?> userListInGroup(@PathVariable("id")Long groupId){
        List<GroupUserDisplay> userList = groupService.getAllUserFromGroup(groupId);

        if(userList != null && !userList.isEmpty()){
            return new ResponseEntity<>(userList,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get-all-userRequest/{id}")
    public ResponseEntity<?> getAllUserRequest(@PathVariable("id")Long groupId,Principal principal){
        List<InvitationDto> invitationDtoList = groupService.getAllUserRequest(principal,groupId);
        if(invitationDtoList != null && !invitationDtoList.isEmpty()){
            return new ResponseEntity<>(invitationDtoList,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/group/getStatus")
    public ResponseEntity<?> getStatusFromAdmin(@RequestBody GroupUserDisplay groupUserDisplay){
        if(groupUserDisplay.getStatus().equals("ACCEPT")){
            groupService.saveUserToGroup(groupUserDisplay.getUserId(),groupUserDisplay.getGroupId());
            userInvitationService.removeUserRequest(groupUserDisplay.getUserId(),groupUserDisplay.getGroupId());
        }else{
            userInvitationService.removeUserRequest(groupUserDisplay.getUserId(),groupUserDisplay.getGroupId());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/kickUser")
    public ResponseEntity<String> kickUser(@RequestBody UserKickForm userKickForm) {
        try {
            log.info("llll {}",userKickForm.getUserId());
            log.info("kkkk {}",userKickForm.getGroupId());
            groupService.kickUser(userKickForm.getUserId(), userKickForm.getGroupId());

            return ResponseEntity.ok("User kicked successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or group not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to kick user: " + e.getMessage());
        }
    }
    @PostMapping("/changeAdmin")
    public ResponseEntity<?> changeAdmin(@RequestBody UserKickForm userKickForm) {

        log.info("NewOnwerId {}",userKickForm.getUserId());
        User user = userService.findById(userKickForm.getUserId());
        if (user != null) {
            Optional<Group> groupOptional = groupService.findById(userKickForm.getGroupId());
            Long previousAdmin = groupOptional.get().getGroupOwner().getId();
            log.info("Previous User Id {}",previousAdmin);

            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();
                group.setGroupOwner(user);
                Group newGroup = groupService.save(group);

                GroupDto newGroupDto = new GroupDto(newGroup);
                GroupUserDisplay changeUser = groupService.getNewUser(userKickForm.getGroupId(),previousAdmin);
                changeUser.setNewAdmin(userKickForm.getUserId());
                return new ResponseEntity<>(changeUser,HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Group not found"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
        }
    }

    @PostMapping("/changeOwner")
    public ResponseEntity<?> changeAdmin(@RequestParam("ownerId") Long ownerId, @RequestParam("communityId") Long communityId) {

        log.info("NewOnwerId {}",ownerId);
        User user = userService.findById(ownerId);
        if (user != null) {
            Optional<Group> groupOptional = groupService.findById(communityId);
            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();
                group.setGroupOwner(user);
                Group newGroup = groupService.save(group);
                GroupDto newGroupDto = new GroupDto(newGroup);
                return new ResponseEntity<>(newGroupDto,HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Group not found"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
        }
    }

    @PostMapping("/rebuild-group/{groupId}")
    public ResponseEntity<String> rebuildGroup(@PathVariable Long groupId) {
        try {
            groupService.toggleGroupStatus(groupId); // Toggle the group status
            return ResponseEntity.ok("Group status toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to toggle group status: " + e.getMessage());
        }
    }
}
