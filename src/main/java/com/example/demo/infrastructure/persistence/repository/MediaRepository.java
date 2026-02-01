package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Media;
import com.example.demo.enumeration.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByContent_Id(Long contentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Media m WHERE m.content.id = :contentId AND m.url = :url")
    void deleteByContentIdAndUrl(@Param("contentId") Long contentId, @Param("url") String url);
}
