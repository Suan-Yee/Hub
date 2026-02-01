package com.example.demo.application.usecase.impl;

import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasSkill;
import com.example.demo.infrastructure.persistence.repository.SkillRepository;
import com.example.demo.infrastructure.persistence.repository.UserHasSkillRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.SkillService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final UserHasSkillRepository userHasSkillRepository;


    @Override
    public Boolean addSkill(Long userId, List<String> skillNames) {
        for (String skillName : skillNames) {
            Skill skill = skillRepository.findByName(skillName);
            if (skill == null) {
                skill = new Skill();
                skill.setName(skillName);
                skillRepository.save(skill);
            }
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // Check if the user already has the skill
                boolean userHasSkill = userHasSkillRepository.existsByUserIdAndSkillId(user.getId(), skill.getId());
                if (!userHasSkill) {
                    // Create a user_has_skill entry
                    UserHasSkill userSkill = new UserHasSkill();
                    userSkill.setSkill(skill);
                    userSkill.setUser(user);
                    userHasSkillRepository.save(userSkill);
                }

            } else {
                return false;
            }
        }
        return true;
    }

    /*find by skill names*/
    @Override
    public List<String> findSkillByUserId(Long userId) {
        return userHasSkillRepository.findSkillwithUserId(userId);
    }

    @Override
    @Transactional
    public void deletebyUserId(Long userId) {
        userHasSkillRepository.deleteByUserId(userId);
    }

}

