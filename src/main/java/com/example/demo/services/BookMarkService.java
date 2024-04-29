package com.example.demo.services;

import com.example.demo.dto.BookMarkDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.BookMark;
import com.example.demo.entity.User;


import java.util.List;

public interface BookMarkService {

    BookMark saveBookMark(BookMarkDto bookMarkDto);
    List<PostDto> findBookMarkPost(User user, boolean status);
    BookMark findByUserAndPost(Long postId);
}
