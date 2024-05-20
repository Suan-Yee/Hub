package com.example.demo.services.impl;

import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRequestGroup;
import com.example.demo.repository.UserRequestGroupRepository;
import com.example.demo.services.UserRequestGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRequestGroupServiceImpl implements UserRequestGroupService {

    private final UserRequestGroupRepository userRequestGroupRepository;

    @Override
    public void createUserRequestGroup(User user, Group group) {
        UserRequestGroup userRequestGroup=new UserRequestGroup();
        userRequestGroup.setUser(user);
        userRequestGroup.setGroup(group);
        userRequestGroupRepository.save(userRequestGroup);
    }

    @Override
    public List<UserRequestGroup> getRequestGroupsByUser(Long userId) {
        return userRequestGroupRepository.findByUserId((long) Math.toIntExact(userId));
    }

    @Override
    public void deleteUserRequestGroup(Long id) {
        userRequestGroupRepository.deleteById(id);
    }

    @Override
    public UserRequestGroup getById(Long id) {
        return userRequestGroupRepository.findById(id)
                .orElse(null);
    }


    @Override
    public void updateHasConfirmed(UserRequestGroup userRequestGroup) {
        userRequestGroup.setHasConfirmed(true);
        userRequestGroupRepository.save(userRequestGroup);
    }

    @Override
    public List<UserRequestGroup> getAllRequestGroups() {
        return userRequestGroupRepository.findAll();
    }
}
