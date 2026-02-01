package com.example.demo.dto;

public record UserRequestGroupDto(
        Long user,
        Long group
) {
    public UserRequestGroupDto() {
        this(null, null);
    }

    public Long getUser() {
        return user;
    }

    public Long getGroup() {
        return group;
    }
}
