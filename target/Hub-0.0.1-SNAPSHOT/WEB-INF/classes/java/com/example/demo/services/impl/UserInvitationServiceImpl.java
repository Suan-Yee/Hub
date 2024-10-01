package com.example.demo.services.impl;

import com.example.demo.dto.UserInvitationDto;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.UserInvitation;
import com.example.demo.exception.CommunityHubException;
import com.example.demo.repository.UserInvitationRepository;
import com.example.demo.services.GroupService;
import com.example.demo.services.UserHasGroupService;
import com.example.demo.services.UserInvitationService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserInvitationRepository userInvitationRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final UserHasGroupService userHasGroupService;

    @Transactional
    @Override
    public void save(Long id, UserInvitationDto userInvitationDto) {
        if (userInvitationDto.getCommunityId() == null) {
            throw new IllegalArgumentException("Community ID must not be null");
        }

        var community = groupService.getCommunityByIds(userInvitationDto.getCommunityId());
        if (community == null) {
            throw new IllegalArgumentException("Community not found for ID: " + userInvitationDto.getCommunityId());
        }

        for (Long userId : userInvitationDto.getUserIds()) {
            var invite = userInvitationRepository.findByRecipientIdAndGroupId(userId, community.getId());
            if (invite == null) {
                var invitation = UserInvitation.builder()
                        .senderId(id)
                        .group(community)
                        .recipientId(userId)
                        .isInvited(true)
                        .isAccepted(false)
                        .isRemoved(false)
                        .build();
                userInvitationRepository.save(invitation);
            } else {
                invite.setRemoved(false);
                invite.setAccepted(false);
                invite.setInvited(true);
                userInvitationRepository.save(invite);
            }
        }
    }


    @Override
    public List<UserInvitation> findLoginUserInvitation(Long id) {
        return userInvitationRepository.findByRecipientIdAndIsInvited(id,true);
    }

    @Transactional
    @Override
    public void findById(Long id) {
        userInvitationRepository.findById(id).ifPresent(i -> {
            i.setInvited(false);
            i.setAccepted(false);
            i.setRemoved(true);
            userInvitationRepository.save(i);
        });
    }

    @Transactional
    @Override
    public void acceptedInvitation(Long id, Long communityId) {
        userInvitationRepository.findById(id).ifPresent(i -> {
            i.setInvited(false);
            i.setRemoved(false);
            i.setAccepted(true);
            userInvitationRepository.save(i);
        });
        processAcceptInvitation(id,communityId);
    }

    public void processAcceptInvitation(Long id,Long communityId){
        var invitation = userInvitationRepository.findById(id).orElseThrow(() -> new CommunityHubException("Invitation not found exception!!"));
        var user = userService.findById(invitation.getRecipientId());
        var community = groupService.getCommunityByIds(communityId);
        var user_group = userHasGroupService.findByUserIdAndGroupId(user.getId(),community.getId());
        if(user_group == null){
            var groupUser = UserHasGroup.builder()
                    .user(user)
                    .group(community)
                    .build();
            userHasGroupService.save(groupUser);
        }
    }
    @Override
    @Transactional
    public void removeUserRequest(Long userId,Long groupId){
        userInvitationRepository.removeUserRequest(userId,groupId);
    }
}
