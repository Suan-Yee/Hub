package com.example.demo.restController;

import com.example.demo.dto.PostDto;
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
public class PostRestController {

    private final PostService postService;

    @GetMapping()
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
    public ResponseEntity<List<PostDto>> findPosts(@RequestParam(name="topicName",required = false) String topicName,
                                                   @RequestParam(name="search",required = false) String searchResult) {
       List<PostDto> posts = postService.findBySpecification(topicName,searchResult);
       log.info("ALl Posts {}",posts);
       return new ResponseEntity<>(posts,HttpStatus.OK);
    }

}
