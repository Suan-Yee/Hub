package com.example.demo.application.usecase;

import com.example.demo.entity.Skill;

import java.util.List;

public interface SkillService {

   Boolean addSkill(Long userId, List<String> skillNames);
    /*find by skill names*/
   List<String> findSkillByUserId(Long userId);
   void deletebyUserId(Long userId);
}
