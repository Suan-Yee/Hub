package com.example.demo.restController;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.*;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor @Slf4j
public class PostRestController {

    private final PostService postService;
    private final UserService userService;


    @GetMapping
    public ResponseEntity<List<Post>> findAllPosts(){
        List<Post> posts = postService.findAllPosts();

        if(posts != null && !posts.isEmpty()){
            return new ResponseEntity<>(posts,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/{postId}")
    public ResponseEntity<?> findPostById(@PathVariable("postId") Long postId,Principal principal){
        User user = userService.findByStaffId(principal.getName());
        PostDto post = postService.findByIdPost(postId,user.getId());

        if(post != null){
            return new ResponseEntity<>(post,HttpStatus.OK);
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
    public ResponseEntity<Page<PostDto>> findPosts(@RequestParam(name = "topicName", required = false) String topicName,
                                                   @RequestParam(name = "search", required = false) String searchResult,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "5") int size,
                                                   Principal principal) {
        User user = userService.findByStaffId(principal.getName());
        Page<PostDto> posts = postService.findBySpecification(topicName, searchResult, user.getId(), page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.isEmpty() ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(posts);
    }


}
