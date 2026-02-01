package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll,Long> {
    List<Poll> findAllByStatusTrueAndGroupIsNullOrderByIdDesc();
    List<Poll> findAllByStatusTrueAndGroupIdOrderByIdDesc(Long groupId);
}
