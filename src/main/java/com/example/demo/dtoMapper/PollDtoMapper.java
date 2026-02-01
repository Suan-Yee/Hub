package com.example.demo.dtoMapper;

import com.example.demo.dto.PollDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;
import com.example.demo.entity.User;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class PollDtoMapper {
    private final UserService userService;
    public PollDto mapToPollViewObject(Poll poll) {
        Map<Long, String> answers = new HashMap<>();
        List<Integer> weights = new ArrayList<>();
        Set<Long> userVoted = new HashSet<>();

        if (poll.getPollOption() != null) {
            for (PollOption option : poll.getPollOption()) {
                answers.put(option.getId(), option.getName());
                if(option.getUser() != null){
                    weights.add(option.getUser().size()); // Counting the number of votes for each option
                    userVoted.addAll(option.getUser().stream().map(User::getId).collect(Collectors.toList()));// Counting the number of total votes for poll
                }
            }
        }
        long groupId =0L;
        if(poll.getGroup()!=null){
            groupId = poll.getGroup().getId();
        }

        return PollDto.builder()
                .id(poll.getId())
                .question(poll.getDescription())
                .answers(answers)
                .pollCount((long) userVoted.size()) // Counting the distinct users who voted
                .answersWeight(weights)
                .selectedAnswer(-1)
                .expiredDate(expiredDateFormatter(poll.getExpiredAt()))
                .user(new UserDto(poll.getUser()))
                .isLoginUserPoll(isLoginUserPoll(poll.getUser().getId()))
                .groupId(groupId)
                .build();
    }
    private String expiredDateFormatter(LocalDateTime expiredDate){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = LocalDateTime.parse(""+expiredDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Duration duration = Duration.between(now,expirationDate);
        if (!duration.isNegative()) {
            long daysDifference = duration.toDays();
            if (daysDifference > 1) {
                return daysDifference + " days left";
            } else if (daysDifference == 1) {
                return "1 day left";
            } else {// Assuming that if daysDifference is less than 1 poll might be expired in hours
                long hoursDifference = duration.toHours();
                if(hoursDifference >1){
                    return hoursDifference + " hours left";
                } else if (hoursDifference ==1)
                    return "1 hour left";
                else{ // Assuming that if hoursDifference is less than 1 poll might be expired in minutes
                    long minutesDifference = duration.toMinutes();
                    return minutesDifference + " minutes left";
                }
            }
        }
        return "expired";
    }
    private Boolean isLoginUserPoll(Long pollId){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(pollId == user.getId()){
            return true;
        }
        return false;
    }
}
