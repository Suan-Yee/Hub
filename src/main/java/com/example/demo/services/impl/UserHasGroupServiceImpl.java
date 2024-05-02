package com.example.demo.services.impl;

import com.example.demo.entity.UserHasGroup;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserHasGroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserHasGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHasGroupServiceImpl implements UserHasGroupService {

    private final UserHasGroupRepository userHasGroupRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserHasGroup> findByGroupId(Long id) {
        return userHasGroupRepository.findByGroupId(id);
    }
}
