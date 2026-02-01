package com.example.demo.dto.response;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public record UserListResponse(List<UserDto> users, int currentPage, int totalPages, long totalItems,
                               int startCount, int endCount, String pageUrlPrefix, String pageUrlPostfix) {

    public static UserListResponse from(Page<User> page, String pageUrlPrefix, String pageUrlPostfix) {
        List<UserDto> dtos = page.getContent().stream().map(UserDto::new).toList();
        int pageNo = page.getNumber() + 1;
        int pageSize = page.getSize();
        int startCount = (pageNo - 1) * pageSize + 1;
        int endCount = Math.min(pageNo * pageSize, (int) page.getTotalElements());
        return new UserListResponse(
                dtos,
                pageNo,
                page.getTotalPages(),
                page.getTotalElements(),
                startCount,
                endCount,
                pageUrlPrefix,
                pageUrlPostfix
        );
    }
}
