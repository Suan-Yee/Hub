package com.example.demo.services;

import com.example.demo.dto.PollOptionReportDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;

import java.util.List;
import java.util.Map;

public interface PollOptionService {
    PollOption save(PollOption pollOption);
    PollOption findPollOptionById(Long pollOptionId);
    Long findPollOptionIdByUserIdAndPollId(Long userId,Long pollId);
    Map<String, Object> deleteUserVotedOptionByAnswerIdAndUserId(long answerId, long userId, long pollId);
    boolean checkUserHaveAlreadyVoted(long pollOptionId,long userId);
    List<UserDto> findUsersByPollOptionId(long pollOptionId);
}
