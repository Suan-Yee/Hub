package com.example.demo.dto.response;

import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;

import java.util.List;

public record UserProfileResponse(Long id, String staffId, String name, String department, String photo,
                                  String biography, Role role, List<String> skills) {
    public static UserProfileResponse from(User user, List<String> skills) {
        return new UserProfileResponse(
                user.getId(),
                user.getStaffId(),
                user.getName(),
                user.getDepartment(),
                user.getPhoto(),
                user.getBiography(),
                user.getRole(),
                skills
        );
    }
}
