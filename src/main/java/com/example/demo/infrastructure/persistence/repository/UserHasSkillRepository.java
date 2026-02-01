package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.UserHasSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserHasSkillRepository extends JpaRepository<UserHasSkill,Long> {
    boolean existsByUserIdAndSkillId(Long userId, Long skillId);

    /*find by skill names*/
    @Query("SELECT us.skill.name FROM User u INNER JOIN UserHasSkill us ON u.id = us.user.id WHERE u.id = :userId")
    List<String> findSkillwithUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserHasSkill uhs WHERE uhs.user.id = :userId")
    void deleteByUserId( Long userId);

}
