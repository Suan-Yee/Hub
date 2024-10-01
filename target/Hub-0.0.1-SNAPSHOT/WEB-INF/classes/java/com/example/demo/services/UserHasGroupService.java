package com.example.demo.services;


import com.example.demo.dto.UserDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;

import java.util.List;

public interface UserHasGroupService {

    List<UserDto> findByGroupId(Long id);

    List<UserDto> findUsersByGroupId(Long groupId);

    Long totalMembers(Long groupId);

    void addUserToGroup(User user, Group group);

    UserHasGroup findByUserIdAndGroupId(Long id, Long id1);

    void save(UserHasGroup groupUser);


}
