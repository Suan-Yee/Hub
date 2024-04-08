package com.example.demo.controllers;

import com.example.demo.entity.Post;
import com.example.demo.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor @Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<Post>> findAllPosts(){
        List<Post> posts = postService.findAllPosts();

        if(posts != null && !posts.isEmpty()){
            return new ResponseEntity<>(posts,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/user/{name}")
    public ResponseEntity<List<Post>> findByUserName(@PathVariable("name")String userName) {
        List<Post> posts = postService.findByUserName(userName);

        if (posts != null && !posts.isEmpty()) {
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/topic/{name}")
    public ResponseEntity<List<Post>> findByTopicName(@PathVariable("name")String topicName){
        List<Post> posts = postService.findByTopicName(topicName);

        if(posts != null && !posts.isEmpty()){
            return new ResponseEntity<>(posts,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
   @GetMapping("/group/{id}")
   public ResponseEntity<List<Post>> findByTopicName(@PathVariable("id")Long groupId){
       List<Post> posts = postService.findByGroupId(groupId);

       if(posts != null && !posts.isEmpty()){
           return new ResponseEntity<>(posts,HttpStatus.OK);
       }else{
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
   }
    @GetMapping("/all")
    public ResponseEntity<List<Post>> findPosts(@RequestParam(name="topicName",required = false) String topicName,
                                @RequestParam(name="userName",required = false) String userName) {
       List<Post> posts = postService.findBySpecification(topicName,userName);

       return new ResponseEntity<>(posts,HttpStatus.OK);
    }
}
