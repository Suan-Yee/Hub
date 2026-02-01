package com.example.demo.dto;

import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.enumeration.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record UserDto(
        Long id,
        String staffId,
        String name,
        String dob,
        String email,
        String department,
        String photo,
        String biography,
        boolean status,
        Role role,
        @JsonIgnore List<UserHasGroup> userHasGroup
) {
    public UserDto(User user) {
        this(
                user.getId(),
                user.getStaffId(),
                user.getName(),
                user.getDob(),
                user.getEmail(),
                user.getDepartment(),
                user.getPhoto(),
                user.getBiography(),
                user.isStatus(),
                user.getRole(),
                user.getGroups()
        );
    }

    public UserDto(String staffId, String name, String department, Role role, String photo, Long id) {
        this(id, staffId, name, null, null, department, photo, null, false, role, null);
    }

    public Long getId() {
        return id;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhoto() {
        return photo;
    }

    public String getBiography() {
        return biography;
    }

    public boolean isStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public List<UserHasGroup> getUserHasGroup() {
        return userHasGroup;
    }
}
