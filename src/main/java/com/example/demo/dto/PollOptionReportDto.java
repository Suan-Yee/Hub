package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PollOptionReportDto {
   private String name;
   private List<UserDto> users;
   private long voteCount;
}
