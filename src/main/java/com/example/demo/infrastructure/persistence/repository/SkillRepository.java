package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Skill;
import com.example.demo.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill,Long> {

    Skill findByName(String name);
}
