package com.example.demo.application.usecase;

import com.example.demo.dto.PollOptionReportDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;
import com.example.demo.entity.User;

import java.util.List;
import java.util.Map;

public interface PollOptionService {
    PollOption save(PollOption pollOption);
    PollOption findPollOptionById(Long pollOptionId);
    Long findPollOptionIdByUserIdAndPollId(Long userId,Long pollId);
    Map<String, Object> deleteUserVotedOptionByAnswerIdAndUserId(long answerId, long userId, long pollId);
    boolean checkUserHaveAlreadyVoted(long pollOptionId,long userId);
    List<UserDto> findUsersByPollOptionId(long pollOptionId);
    /**
     * Add a user vote to a poll option (transactional; accesses lazy collection).
     */
    boolean addUserVote(Long pollOptionId, User user);
}
