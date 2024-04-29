package com.example.demo.services.impl;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.*;
import com.example.demo.enumeration.LikeType;
import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.repository.LikeRepository;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final PostService postService;
    private final UserService userService;
    private final NotificationService notificationService;
    private static String LikeNoti = " liked your post";

    public boolean saveLike(LikeDto likeDto, Principal principal) {
        Post post = postService.findById(likeDto.getPostId());
        User user = userService.findByStaffId(principal.getName());

        final boolean[] isNewLike = new boolean[1];
        Like like = likeRepository.findLikePost(post.getId(), user.getId())
                .orElseGet(() -> {
                    isNewLike[0] = true;
                    if (!Objects.equals(user.getId(), post.getUser().getId())) {
                        notificationService.updateOrCreateNotificationPost(post, user, true);
                    }
                    return createNewLike(user, post);
                });

        if (!like.isLikeStatus() || isNewLike[0]) {
            like.setLikeStatus(true);
            likeRepository.save(like);

            if(!Objects.equals(user.getId(), post.getUser().getId())){
                notificationService.updateOrCreateNotificationPost(post,user,true);
                notificationService.notifyUser(post.getUser().getStaffId(),user.getName() + LikeNoti );
            }
            return true;
        } else {
            like.setLikeStatus(false);
            if (!Objects.equals(user.getId(), post.getUser().getId())) {
                notificationService.updateOrCreateNotificationPost(post, user, false);
            }
            likeRepository.save(like);
            return false;
        }
    }

    @Override
    public Like createNewLike(User user,Post post){
        return Like.builder().user(user).post(post).likeStatus(true).build();
    }

    @Override
    public Long totalLikePost(Long postId) {
        return likeRepository.totalLikePost(postId).orElse(0L);
    }
}
