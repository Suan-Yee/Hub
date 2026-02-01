package com.example.demo.dto.response;

import com.example.demo.dto.GroupDto;

import java.util.List;

public record GroupRequestResponse(List<GroupDto> groups, String message, String alertClass) {}
