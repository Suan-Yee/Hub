package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content,Long> {
}
