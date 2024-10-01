package com.example.demo.repository;

import com.example.demo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface VideoRepository extends JpaRepository<Video,Long> {

    Video findByContent_Id(Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Video v WHERE v.content.id = :contentId AND v.name = :videoUrl")
    void deleteByContentId(@Param("videoUrl") String videoUrl, @Param("contentId") Long contentId);
}
