package com.example.demo.controller;

import com.example.demo.dto.UserRequestGroupDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRequestGroup;
import com.example.demo.services.GroupService;
import com.example.demo.services.UserHasGroupService;
import com.example.demo.services.UserRequestGroupService;
import com.example.demo.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserRequestGroupController {

    private final UserService userService;
    private final GroupService groupService;
    private final UserRequestGroupService userRequestGroupService;
    private final UserHasGroupService userHasGroupService;

    @GetMapping("/createGroupRequest")
    public ModelAndView requestGroupView(Model model){
        model.addAttribute("groups",groupService.findAllGroup());
        return new ModelAndView("userRequestGroup","requestGroup",new UserRequestGroupDto());

    }

    @PostMapping("/createGroupRequest")
    public ModelAndView requestGroup(@ModelAttribute("requestGroup") UserRequestGroupDto userRequestGroupDto, RedirectAttributes redirectAttributes, HttpSession session){
        if (userRequestGroupDto.getGroup() == 0) {
            redirectAttributes.addFlashAttribute("needGroup", "Need to select a group!");
            return new ModelAndView("redirect:/createGroupRequest");
        }

        System.out.println(userRequestGroupDto);
        User user = userService.findById((Long) session.getAttribute("userId"));
        Group group = groupService.getCommunityBy(userRequestGroupDto.getGroup());
        userRequestGroupService.createUserRequestGroup(user, group);

        redirectAttributes.addFlashAttribute("groupRequestMessage", "Group request created successfully!");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        return new ModelAndView("redirect:/createGroupRequest").addObject("id", user.getId());
    }

    @GetMapping("/groupRequest/cancel/{id}")
    public ModelAndView groupCancel(@PathVariable("id")Long id){
        System.out.println(id);
        userRequestGroupService.deleteUserRequestGroup(id);
        return new ModelAndView("redirect:/createGroupRequest");
    }

    @GetMapping("/groupRequest/accept")
    public ModelAndView groupAccept(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            UserRequestGroup requestGroup = userRequestGroupService.getById(id);
            if (requestGroup != null && !requestGroup.isHasConfirmed()) {
                userRequestGroupService.updateHasConfirmed(requestGroup);

                User user = requestGroup.getUser();
                Group group = requestGroup.getGroup();

                userHasGroupService.addUserToGroup(user, group);

                redirectAttributes.addFlashAttribute("accepted", "User added to group successfully!");
                redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            } else {
                redirectAttributes.addFlashAttribute("accepted", "This request group is already accepted!");
                redirectAttributes.addFlashAttribute("alertClass", "alert-warning");
            }
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("accepted", "Error occurred while accepting the request!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return new ModelAndView("redirect:/profile");
    }

    @GetMapping("/groupRequest/delete")
    public ModelAndView groupDelete(@RequestParam("id")Long id,RedirectAttributes redirectAttributes){
        if(userRequestGroupService.getById(id).isHasConfirmed()){
            userRequestGroupService.deleteUserRequestGroup(id);
        }else{
            redirectAttributes.addFlashAttribute("cantDeleteWithoutAccept", "You must first need to click accept button!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }
        return new ModelAndView("redirect:/profile");
    }


}









