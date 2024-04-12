package com.example.demo.services;

import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;

public interface CommentService {

    Comment saveComment(Comment comment);
}
