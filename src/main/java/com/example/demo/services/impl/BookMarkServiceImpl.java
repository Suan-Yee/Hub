package com.example.demo.services.impl;

import com.example.demo.dto.BookMarkDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.BookMark;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.BookMarkRepository;
import com.example.demo.services.BookMarkService;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
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
        List<BookMark> bookMarks = bookMarkRepository.findByUserAndStatus(user,status);
        if(bookMarks.isEmpty()){
            return Collections.emptyList();
        }
        return bookMarks.stream().map(bookMark -> new PostDto(bookMark.getPost())).collect(Collectors.toList());
    }

    @Override
    public BookMark findByUserAndPost(Long postId) {
        return bookMarkRepository.findByUserAndPost(postId).orElse(null);
    }

    private BookMark createNewBookMark(BookMarkDto bookMarkDto){

        User user = userService.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName());
        Post post = postService.findById(bookMarkDto.getPostId());
        return BookMark.builder().user(user).post(post).status(true).build();
    }
}
