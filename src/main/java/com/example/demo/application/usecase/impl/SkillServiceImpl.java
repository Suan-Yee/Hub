package com.example.demo.application.usecase.impl;

import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.SkillRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.SkillService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public Boolean addSkill(Long userId, List<String> skillNames) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        if (user.getSkills() == null) {
            user.setSkills(new ArrayList<>());
        }

        for (String rawSkillName : skillNames) {
            if (rawSkillName == null || rawSkillName.isBlank()) {
                continue;
            }
            String skillName = rawSkillName.trim();
            Skill skill = skillRepository.findByName(skillName);
            if (skill == null) {
                skill = new Skill();
                skill.setName(skillName);
                skill = skillRepository.save(skill);
            }

            String targetName = skill.getName();
            boolean alreadyAssigned = targetName != null && user.getSkills().stream()
                    .map(Skill::getName)
                    .filter(Objects::nonNull)
                    .anyMatch(existingName -> existingName.equalsIgnoreCase(targetName));
            if (targetName != null && !alreadyAssigned) {
                user.getSkills().add(skill);
            }
        }

        userRepository.save(user);
        return true;
    }

    /*find by skill names*/
    @Override
    public List<String> findSkillByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSkills() == null
                        ? List.<String>of()
                        : user.getSkills().stream()
                            .map(Skill::getName)
                            .filter(name -> name != null && !name.isBlank())
                            .toList())
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public void deletebyUserId(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getSkills() != null) {
                user.getSkills().clear();
                userRepository.save(user);
            }
        });
    }

}

