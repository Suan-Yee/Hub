package com.example.demo.application.usecase.impl;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.infrastructure.persistence.repository.GroupRepository;
import com.example.demo.infrastructure.persistence.repository.UserHasGroupRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.UserHasGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHasGroupServiceImpl implements UserHasGroupService {

    private final UserHasGroupRepository userHasGroupRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserDto> findByGroupId(Long id) {
        List<User> userList = userHasGroupRepository.findByGroupId(id);
        return userList.stream().map(UserDto::new).toList();

    }

    @Override
    public List<UserDto> findUsersByGroupId(Long groupId){

        List<User> userList = userHasGroupRepository.findUsersByGroupId(groupId);
        List<UserDto> userDtoList = userList.stream().map(UserDto::new).toList();
        return userDtoList;
    }

    @Override
    public Long totalMembers(Long groupId) {
        return userHasGroupRepository.totalMembers(groupId);
    }

    @Override
    @Transactional
    public void addUserToGroup(User user, Group group) {
        UserHasGroup userHasGroup = new UserHasGroup();
        userHasGroup.setUser(user);
        userHasGroup.setGroup(group);

        // Save the association
        userHasGroupRepository.save(userHasGroup);

    }

    @Override
    public UserHasGroup findByUserIdAndGroupId(Long id, Long id1) {
        return userHasGroupRepository.findByUserIdAndGroupId(id,id1);
    }
    @Transactional
    @Override
    public void save(UserHasGroup groupUser) {
        userHasGroupRepository.save(groupUser);
    }
}
