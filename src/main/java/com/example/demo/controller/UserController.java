package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String userProfile(ModelMap model){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        model.addAttribute("User",user);
        return "admin_profile";
    }

    @GetMapping("/list")
    public String defaultUserList(ModelMap model) {
        return userList(model, 1);
    }
    @GetMapping("/list/{pageNo}")
    public String userList(ModelMap m,@PathVariable(value = "pageNo") int pageNo) {
        int pageSize = 10;
        Page<User> page = userService.findAllUser(pageNo, pageSize);
        List<User> list =page.getContent();
        m.addAttribute("currentPage", pageNo);
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("totalItems", page.getTotalElements());
        m.addAttribute("list",list);
        int startCount = (pageNo - 1) * pageSize + 1;
        int endCount = Math.min(pageNo * pageSize, (int) page.getTotalElements());

        m.addAttribute("startCount", startCount);
        m.addAttribute("endCount", endCount);
        m.addAttribute("pageUrlPrefix", "/list/");
        m.addAttribute("pageUrlPostfix", "");
        return "user-listing";
    }

    @GetMapping("/userprofile/{id}")
    public  String userProfileDetails(@PathVariable("id") Long userId, ModelMap m){
        User user =userService.findById(userId);
        m.addAttribute("list",user);
        return "userprofiledetail";
    }

    @GetMapping("/searchusers")
    public String searchUsers(@RequestParam(name = "query") String query,
                              @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                              ModelMap model) {
        int pageSize = 10; // Change this to your desired page size
        Page<User> usersPage = userService.searchUsers(query, pageNo, pageSize);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("startCount", (pageNo - 1) * pageSize + 1);
        model.addAttribute("endCount", Math.min(pageNo * pageSize, usersPage.getTotalElements()));
        model.addAttribute("pageUrlPrefix", "/searchusers/");
        model.addAttribute("pageUrlPostfix", "");
        model.addAttribute("query", query);
        return "user-listing";
    }

}
