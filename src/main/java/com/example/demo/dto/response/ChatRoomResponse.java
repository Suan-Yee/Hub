package com.example.demo.dto.response;

import com.example.demo.dto.GroupDto;
import com.example.demo.entity.User;

public record ChatRoomResponse(GroupDto groups, User user) {}
