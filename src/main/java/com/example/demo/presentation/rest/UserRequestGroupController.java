package com.example.demo.presentation.rest;

import com.example.demo.dto.GroupDto;
import com.example.demo.dto.UserRequestGroupDto;
import com.example.demo.dto.response.GroupRequestResponse;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRequestGroup;
import com.example.demo.application.usecase.GroupService;
import com.example.demo.application.usecase.UserHasGroupService;
import com.example.demo.application.usecase.UserRequestGroupService;
import com.example.demo.application.usecase.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserRequestGroupController {

    private final UserService userService;
    private final GroupService groupService;
    private final UserRequestGroupService userRequestGroupService;
    private final UserHasGroupService userHasGroupService;

    @GetMapping("/createGroupRequest")
    public ResponseEntity<GroupRequestResponse> requestGroupView() {
        return ResponseEntity.ok(new GroupRequestResponse(groupService.findAllGroup(), null, null));
    }

    @PostMapping("/createGroupRequest")
    public ResponseEntity<GroupRequestResponse> createGroupRequest(@RequestBody UserRequestGroupDto userRequestGroupDto, HttpSession session) {
        if (userRequestGroupDto.getGroup() == null || userRequestGroupDto.getGroup() == 0) {
            return ResponseEntity.badRequest().body(new GroupRequestResponse(groupService.findAllGroup(), "Need to select a group!", null));
        }
        User user = userService.findById((Long) session.getAttribute("userId"));
        Group group = groupService.getCommunityBy(userRequestGroupDto.getGroup());
        userRequestGroupService.createUserRequestGroup(user, group);
        return ResponseEntity.ok(new GroupRequestResponse(groupService.findAllGroup(), "Group request created successfully!", "alert-success"));
    }

    @DeleteMapping("/groupRequest/cancel/{id}")
    public ResponseEntity<GroupRequestResponse> groupCancel(@PathVariable("id") Long id) {
        userRequestGroupService.deleteUserRequestGroup(id);
        return ResponseEntity.ok(new GroupRequestResponse(groupService.findAllGroup(), "Request cancelled", "alert-success"));
    }

    @PostMapping("/groupRequest/accept")
    public ResponseEntity<GroupRequestResponse> groupAccept(@RequestParam("id") Long id) {
        try {
            UserRequestGroup requestGroup = userRequestGroupService.getById(id);
            if (requestGroup != null && !requestGroup.isHasConfirmed()) {
                userRequestGroupService.updateHasConfirmed(requestGroup);
                User user = requestGroup.getUser();
                Group group = requestGroup.getGroup();
                userHasGroupService.addUserToGroup(user, group);
                return ResponseEntity.ok(new GroupRequestResponse(null, "User added to group successfully!", "alert-success"));
            }
            return ResponseEntity.ok(new GroupRequestResponse(null, "This request group is already accepted!", "alert-warning"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GroupRequestResponse(null, "Error occurred while accepting the request!", "alert-danger"));
        }
    }

    @DeleteMapping("/groupRequest/delete")
    public ResponseEntity<GroupRequestResponse> groupDelete(@RequestParam("id") Long id) {
        UserRequestGroup request = userRequestGroupService.getById(id);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        if (request.isHasConfirmed()) {
            userRequestGroupService.deleteUserRequestGroup(id);
            return ResponseEntity.ok(new GroupRequestResponse(null, "Request deleted", "alert-success"));
        }
        return ResponseEntity.badRequest().body(new GroupRequestResponse(null, "You must first click accept button!", "alert-warning"));
    }
}









