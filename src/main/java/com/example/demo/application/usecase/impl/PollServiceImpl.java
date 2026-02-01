package com.example.demo.application.usecase.impl;

import com.example.demo.dto.PollDto;
import com.example.demo.dtoMapper.PollDtoMapper;
import com.example.demo.entity.Poll;
import com.example.demo.infrastructure.persistence.repository.PollOptionRepository;
import com.example.demo.infrastructure.persistence.repository.PollRepository;
import com.example.demo.application.usecase.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    private final PollRepository pollRepository;
    private final PollDtoMapper pollDtoMapper;
    private final PollOptionRepository pollOptionRepository;

    @Override
    public Poll save(Poll poll) {
        poll.setStatus(true);
        return pollRepository.save(poll);
    }

    @Override
    public List<PollDto> findAllPoll() {
        deleteExpiredPolls();
        return pollRepository.findAllByStatusTrueAndGroupIsNullOrderByIdDesc().stream().map(pollDtoMapper::mapToPollViewObject).toList();
    }

    @Override
    public List<PollDto> findAllPollByGroupId(long groupId) {
        deleteExpiredPolls();
        return pollRepository.findAllByStatusTrueAndGroupIdOrderByIdDesc(groupId).stream()
                .map(pollDtoMapper::mapToPollViewObject)
                .toList();
    }

    @Override
    public void changeStatus(long pollId) {
        Poll poll =  pollRepository.findById(pollId).get();
        poll.setStatus(false);
        pollRepository.save(poll);
    }


    private void deleteExpiredPolls(){
        LocalDateTime now = LocalDateTime.now();
        List<Poll> polls = pollRepository.findAll();
        for (Poll poll: polls) {
            if(poll.getExpiredAt().isBefore(now))
                poll.setStatus(false);
            pollRepository.save(poll);
        }
    }

}
