package com.example.demo.repository;

import com.example.demo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video,Long> {

    Video findByContent_Id(Long id);

}
