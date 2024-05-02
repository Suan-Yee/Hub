package com.example.demo.services.impl;


import com.example.demo.dto.GroupDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.exception.CommunityHubException;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserHasGroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.dto.GroupDto.toGroup;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserHasGroupRepository userHasGroupRepository;


    @Override
    public void createCommunity(GroupDto groupDto, Principal principal) {

        var user = userRepository.findByStaffId(principal.getName()).orElseThrow();

        GroupDto resultGroupDto = createGroupDto(groupDto,principal);
        Group group = toGroup(resultGroupDto);
        group.setGroupOwner(user);

        var com = groupRepository.save(group);

        UserHasGroup userHasGroup = new UserHasGroup();
        userHasGroup.setUser(user);
        userHasGroup.setGroup(com);
        userHasGroupRepository.save(userHasGroup);

    }
    private GroupDto createGroupDto(GroupDto groupDto,Principal principal){

        return GroupDto.builder().name(groupDto.getName()).
                description(groupDto.getDescription()).rule(groupDto.getRule())
                .image(groupDto.getImage()).active(true).build();

    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public List<Group> getAllCommunity(Model model) {
        List<Group> communities = groupRepository.findAll();

        return communities;
    }

    @Override
    public List<GroupDto> findAllGroup() {
        List<Group> groupList = groupRepository.findAllGroup();
        log.info("Group list {}",groupList);
        return groupList.stream().map(GroupDto::new).collect(Collectors.toList());
    }

    @Override
    public Group getCommunityBy(Long id) {
        return groupRepository.findById(id).orElseThrow();
    }

    @Override
    public void createGroup(GroupDto group, List<Long> ids) {
        Group group1 = groupRepository.findById(group.getId()).orElse(null);
        group1.setDescription(group.getDescription());
        group1.setRule(group.getRule());

        groupRepository.save(group1);

        for(Long u_id : ids){
            UserHasGroup user_group = new UserHasGroup();
            User user = userRepository.findById(u_id).orElseThrow();
            user_group.setGroup(group1);
            user_group.setUser(user);
            userHasGroupRepository.save(user_group);
        }
    }

    @Override
    public List<String> getOwnerNamesByCommunityId(Long communityId) {
        List<UserHasGroup> userGroups = userHasGroupRepository.findByGroupId(communityId);
        return userGroups.stream().map(userGroup -> userGroup.getUser().getName()).collect(Collectors.toList());
    }

    @Override
    public List<Group> getAllCommunityWithUserId() {
        var list = new ArrayList<UserHasGroup>();
        var user = userRepository.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new CommunityHubException("not found"));
        var ids =    userHasGroupRepository.findDistinctGroupIdByUserId(user.getId());
        var communities = new ArrayList<Group>();
        for(var id : ids){
            communities.add(groupRepository.findById(id).orElseThrow(()->new CommunityHubException("not found group")));
        }
        return  communities;
    }

    @Override
    public Optional<Group> findById(Long communityId) {
        return groupRepository.findById(communityId);
    }

    @Override
    public Group save(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(Long groupId) {
        groupRepository.deleteById(groupId);
    }

//    @Override
//    public void kickGroup(Group group, List<Long> ids) {
//        groupRepository.findById(group.getId()).ifPresent(c -> {
//
//            for (Long u_id : ids) {
//                userHasGroupRepository.deleteByCommunityIdAndUserId(group.getId(), u_id);
//            }
//
//        });
//    }

    @Override
    public void kickGroup(Group group, List<Long> userIds) {
        groupRepository.findById(group.getId()).ifPresent(g -> {
            userIds.forEach(userId -> {
                userHasGroupRepository.deleteByCommunityIdAndUserId(group.getId(), userId);
            });
        });
    }

    @Override
    public Group getCommunityById(Long communityId) {
        return groupRepository.findById(communityId).orElse(null);
    }

    @Override
    public List<Long> findGroupIdsByUserId(Long userId) {
       /* User userOptional = userRepository.findById(userId).orElse(null);
        if (userOptional != null) {
            User user = userOptional.get();
            return user.getGroups().stream().map(UserHasGroup::getId).collect(Collectors.toList());
        } else {
            // Handle case when user is not found
            return Collections.emptyList();
        }*/
       List<Long> groupIds = userGroup(userId);
       return groupIds;
    }
    private List<Long> userGroup(Long userId){
        List<Long> groupId = userHasGroupRepository.groupIdByUserId(userId);
        return groupId;
    }

    @Override
    public GroupDto findGroupsByIds(Long groupIds) {
        Group group = groupRepository.findById(groupIds).orElse(null);
        log.info("Group ID 1 {}",group);
        GroupDto groupDto = new GroupDto();
        groupDto.setId(group.getId());
        groupDto.setImage(group.getImage());
        groupDto.setName(group.getName());

        return groupDto;
    }

    @Override
    public Group findGroupsByRoomId(Long roomId) {
        return groupRepository.findById(roomId).orElse(null);
    }

//    @Transactional
//    @Override
//    public void updateGroup(GroupDto groupDto) {
//        Optional<Group> optionalGroup = groupRepository.findById(groupDto.getId());
//
//        if (optionalGroup.isPresent()) {
//            Group group = optionalGroup.get();
//
//            // Update the group entity with new values from the DTO
//            group.setName(groupDto.getName());
//            group.setDescription(groupDto.getDescription());
//            group.setRule(groupDto.getRule());
//            // Update other properties as needed
//
//            // Save the updated group entity back to the database
//            groupRepository.save(group);
//        } else {
//            // Handle the case where the group with the given ID doesn't exist
//            throw new EntityNotFoundException("Group not found with id: " + groupDto.getId());
//        }
//    }
    }



