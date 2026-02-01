package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.GuideLines;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideLinesRepository extends JpaRepository<GuideLines, Long> {
}
