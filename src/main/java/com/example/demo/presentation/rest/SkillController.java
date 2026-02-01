package com.example.demo.presentation.rest;


import com.cloudinary.api.exceptions.BadRequest;
import com.example.demo.entity.Skill;

import com.example.demo.application.usecase.SkillService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class SkillController {

    private final SkillService skillService;
    private final UserService userService;

    /*@PostMapping("/saveSkills")
    public ResponseEntity<?> createSkill(Principal principal, @RequestBody List<String> skills) {
        log.info("Skill {}",skills);
        if(skills.isEmpty()){
            log.info("Skill is null");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Long userId = userService.findByStaffId(principal.getName()).getId();
        log.info("User ID : {}", userId);
        if(userId !=null){
            if(skillService.addSkill(userId, skills))
            return new ResponseEntity<>(HttpStatus.OK);
            else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }*/

    @PostMapping("/saveSkills")
    public ResponseEntity<?> createSkill(Principal principal, @RequestBody List<String> skills) {
        Long userId = userService.findByStaffId(principal.getName()).getId();
        log.info("User ID: {}", userId);
        Boolean skill = skillService.addSkill(userId, skills);
        if (skill != null) {
            return ResponseEntity.ok(skill);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getUserSkills")
    public List<String> getUserSkills(Principal principal) {
        Long userId = userService.findByStaffId(principal.getName()).getId();
        return skillService.findSkillByUserId(userId);
    }

@PostMapping("/editSkills")
    public ResponseEntity<?> updateSkill(Principal principal,@RequestBody List<String> skills){
        log.info("Editting Skill : {}",skills);
    Long userId = userService.findByStaffId(principal.getName()).getId();
    skillService.deletebyUserId(userId);
    Boolean skill = skillService.addSkill(userId, skills);
    if (skill != null) {
        return ResponseEntity.ok(skill);
    } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

}



