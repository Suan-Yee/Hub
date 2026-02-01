package com.example.demo.application.usecase;

import com.example.demo.dto.PollDto;
import com.example.demo.entity.Poll;

import java.util.List;

public interface PollService {
    Poll save(Poll poll);
    List<PollDto> findAllPoll();
    void changeStatus(long pollId);
    List<PollDto> findAllPollByGroupId(long groupId);

}
