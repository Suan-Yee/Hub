package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PollOptionRepository extends JpaRepository<PollOption,Long> {

    @Query("SELECT po.id FROM PollOption po JOIN po.user u WHERE u.id = :userId AND po.poll.id = :pollId")
    Long findOptionIdByUserIdAndPollId(Long userId,Long pollId);

    @Modifying
    @Query(value = "DELETE  from option_has_user where poll_option_id =:answerId and user_id=:userId",nativeQuery = true)
    int deleteUserVotedOption(long answerId,long userId);

    List<PollOption> findAllByPollId(Long pollId);

    @Query("SELECT COUNT(po.id) > 0 FROM PollOption po JOIN po.user u WHERE po.id = :pollOptionId AND u.id = :userId")
    boolean existsByPollOptionIdAndUserId(long pollOptionId,long userId);


}
