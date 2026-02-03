package com.example.demo.repository;

import com.example.demo.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByUserId(Long userId);
    
    @Query("SELECT s FROM Story s WHERE s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStories(OffsetDateTime now);
    
    @Query("SELECT s FROM Story s WHERE s.expiresAt <= :now")
    List<Story> findExpiredStories(OffsetDateTime now);
}
