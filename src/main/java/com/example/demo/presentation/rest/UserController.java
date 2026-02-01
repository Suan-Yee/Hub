package com.example.demo.presentation.rest;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.response.UserListResponse;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.application.usecase.SkillService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SkillService skillService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> profile(Principal principal) {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        List<String> skills = skillService.findSkillByUserId(user.getId());
        return ResponseEntity.ok(UserProfileResponse.from(user, skills));
    }

    @GetMapping("/list")
    public ResponseEntity<UserListResponse> defaultUserList() {
        return userList(1);
    }

    @GetMapping("/list/{pageNo}")
    public ResponseEntity<UserListResponse> userList(@PathVariable(value = "pageNo") int pageNo) {
        int pageSize = 10;
        Page<User> page = userService.findAllUser(pageNo, pageSize);
        return ResponseEntity.ok(UserListResponse.from(page, "/list/", ""));
    }

    @GetMapping("/userprofile/{id}")
    public ResponseEntity<UserProfileResponse> userProfileDetails(@PathVariable("id") Long userId) {
        User user = userService.findById(userId);
        if (user == null) return ResponseEntity.notFound().build();
        List<String> skills = skillService.findSkillByUserId(userId);
        return ResponseEntity.ok(UserProfileResponse.from(user, skills));
    }

    @GetMapping("/searchusers")
    public ResponseEntity<UserListResponse> searchUsers(@RequestParam(name = "query", defaultValue = "") String query,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") int pageNo) {
        if ("null".equals(query)) query = "";
        int pageSize = 10;
        Page<User> page = userService.searchUsers(query, pageNo, pageSize);
        String prefix = "/searchusers?query=" + (query.isEmpty() ? "" : query) + "&pageNo=";
        return ResponseEntity.ok(UserListResponse.from(page, prefix, ""));
    }

    @GetMapping("/userList")
    public ResponseEntity<List<UserDto>> userList() {
        return ResponseEntity.ok(userService.findByAccess().stream().map(UserDto::new).toList());
    }
}
