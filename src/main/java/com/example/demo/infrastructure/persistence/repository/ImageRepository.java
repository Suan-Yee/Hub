package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {

    List<Image> findByContent_Id(Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.content.id = :contentId AND i.name = :imageUrl")
    void deleteByContentId(@Param("imageUrl") String imageUrl,@Param("contentId") Long contentId);
}
