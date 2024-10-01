package com.example.demo.repository;

import com.example.demo.entity.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic,Long> {

    Optional<Topic> findByNameContaining(String name);

    @Query("SELECT pt.topic FROM PostTopic pt GROUP BY pt.topic ORDER BY COUNT(pt.topic) DESC")
    List<Topic> findTop5TopicsByPostCount(Pageable pageable);
}
