package com.example.demo.dtoMapper;

import com.example.demo.dto.PollOptionReportDto;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.PollOption;
import com.example.demo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PollOptionReportDtoMapper {

    @Transactional(readOnly = true)
    public PollOptionReportDto mapToPollOptionReportDto(PollOption pollOption) {
        List<UserDto> userList = new ArrayList<>();
        for(User user :pollOption.getUser()){
            userList.add(new UserDto(user));
        }
        return PollOptionReportDto.builder()
                .name(pollOption.getName())
                .users(userList)
                .voteCount((long)pollOption.getUser().size())
                .build();
    }
}
