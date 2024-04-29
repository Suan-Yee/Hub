package com.example.demo.controller;

import com.example.demo.dto.BookMarkDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.BookMark;
import com.example.demo.entity.User;
import com.example.demo.services.BookMarkService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/bookmark")
@RequiredArgsConstructor
public class BookMarkController {

    private final BookMarkService bookMarkService;
    private final UserService userService;

    @PostMapping("/save")
    public ResponseEntity<?> saveBookMark(@RequestBody BookMarkDto bookMarkDto){
        BookMark bookMark = bookMarkService.saveBookMark(bookMarkDto);
        boolean status = bookMark.isStatus();
        return new ResponseEntity<>(status,HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> fetchAllBookMarkPost(Principal principal){
        User user = userService.findByStaffId(principal.getName());
        List<PostDto> bookMarkPosts = bookMarkService.findBookMarkPost(user,true);
        if(bookMarkPosts.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bookMarkPosts, HttpStatus.OK);
    }
}
