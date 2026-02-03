package com.example.demo.repository;

import com.example.demo.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, PollVote.PollVoteId> {
    List<PollVote> findByPollOptionId(Long pollOptionId);
    List<PollVote> findByUserId(Long userId);
    Optional<PollVote> findByPollOptionIdAndUserId(Long pollOptionId, Long userId);
    boolean existsByPollOptionIdAndUserId(Long pollOptionId, Long userId);
}
