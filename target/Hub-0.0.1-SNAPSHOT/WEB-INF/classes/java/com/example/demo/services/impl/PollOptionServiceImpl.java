package com.example.demo.services.impl;

import com.example.demo.dto.PollOptionReportDto;
import com.example.demo.dto.UserDto;
import com.example.demo.dtoMapper.PollOptionReportDtoMapper;
import com.example.demo.entity.PollOption;
import com.example.demo.repository.PollOptionRepository;
import com.example.demo.services.PollOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PollOptionServiceImpl implements PollOptionService {

    private final PollOptionRepository pollOptionRepository;
    private final PollOptionReportDtoMapper pollOptionReportDtoMapper;

    @Override
    public PollOption save(PollOption pollOption) {
        return pollOptionRepository.save(pollOption);
    }

    @Override
    public PollOption findPollOptionById(Long pollOptionId) {
        return pollOptionRepository.findById(pollOptionId).orElse(null);
    }

    @Override
    public Long findPollOptionIdByUserIdAndPollId(Long userId,Long pollId) {
        return pollOptionRepository.findOptionIdByUserIdAndPollId(userId,pollId);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteUserVotedOptionByAnswerIdAndUserId(long answerId, long userId, long pollId) {
         int result = pollOptionRepository.deleteUserVotedOption(answerId,userId);
         Map<String, Object> response = new HashMap<>();
         if(result>0){
             List<PollOption> options = pollOptionRepository.findAllByPollId(pollId);
             int totalVotes = options.stream().mapToInt(option -> option.getUser().size()).sum();
             List<Map<String, String>> percentages = options.stream()
                     .map(option -> {
                         Map<String, String> data = new HashMap<>();
                         data.put("optionId", String.valueOf(option.getId()));
                         data.put("percentage", totalVotes > 0 ? String.format("%.2f", (option.getUser().size() * 100.0) / totalVotes) + "%" : "0%");
                         return data;
                     }).collect(Collectors.toList());

             response.put("success", true);
             response.put("percentages", percentages);
         } else {
             response.put("success", false);
         }
        return response;
    }

    @Override
    public boolean checkUserHaveAlreadyVoted(long pollOptionId, long userId) {
        return pollOptionRepository.existsByPollOptionIdAndUserId(pollOptionId,userId);
    }

    @Override
    public List<UserDto> findUsersByPollOptionId(long pollOptionId) {
        Optional<PollOption> pollOption = pollOptionRepository.findById(pollOptionId);
        if(pollOption.isPresent()){
            return pollOption.get().getUser().stream().map(UserDto::new).collect(Collectors.toList());
        }else{
            throw new RuntimeException("PollOption not found for id :: " + pollOptionId);
        }
    }
}
