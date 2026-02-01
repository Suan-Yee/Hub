package com.example.demo.application.usecase.impl;

import com.example.demo.dto.BookMarkDto;
import com.example.demo.dto.PostDto;
import com.example.demo.dto.TopicDto;
import com.example.demo.entity.BookMark;
import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.BookMarkRepository;
import com.example.demo.application.usecase.BookMarkService;
import com.example.demo.application.usecase.PostService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor @Slf4j
public class BookMarkServiceImpl implements BookMarkService {

    private final BookMarkRepository bookMarkRepository;
    private final UserService userService;
    private final PostService postService;

    @Override
    public BookMark saveBookMark(BookMarkDto bookMarkDto) {
        BookMark bookMark = findByUserAndPost(bookMarkDto.getPostId());

        if(bookMark != null){
           bookMark.setStatus(!bookMark.isStatus());
           return bookMarkRepository.save(bookMark);
        }else{
            BookMark newBookMark = createNewBookMark(bookMarkDto);
            return bookMarkRepository.save(newBookMark);
        }
    }

    @Override
    public List<PostDto> findBookMarkPost(User user, boolean status) {
        List<BookMark> bookMarks = bookMarkRepository.findByUserAndStatus(user.getId(),true);
        if(bookMarks.isEmpty()){
            return Collections.emptyList();
        }
        List<Post> postList = bookMarks.stream().map(BookMark::getPost).toList();
        List<PostDto> postDtoList = postList.stream().map(post -> postService.transformToPostDto(post,user.getId())).toList();
        return postDtoList;
    }

    @Override
    public BookMark findByUserAndPost(Long postId) {
        return bookMarkRepository.findByUserAndPost(postId).orElse(null);
    }

    @Override
    public List<TopicDto> findBookMarkTopic(User user, boolean status) {
        List<PostDto> bookMarkPosts = findBookMarkPost(user, status);

        Map<String, TopicDto> topicMap = bookMarkPosts.stream()
                .map(PostDto::getTopic)
                .collect(Collectors.toMap(
                        TopicDto::getName,
                        topic -> topic,
                        (existing, replacement) -> existing));

        List<TopicDto> topicDtoList = new ArrayList<>(topicMap.values());
//        topicDtoList.forEach(topic -> {
//            topic.setTotalPost(bookMarkRepository.countByBookMarkTopic(topic.getId(),user.getId())));
//        });

        return topicDtoList;
    }


    @Override
    public int totalPosts(User user) {
       List<PostDto> postDtoList = findBookMarkPost(user,true);
        return postDtoList.size();
    }

    private BookMark createNewBookMark(BookMarkDto bookMarkDto){

        User user = userService.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName());
        Post post = postService.findById(bookMarkDto.getPostId());
        return BookMark.builder().user(user).post(post).status(true).build();
    }
}
