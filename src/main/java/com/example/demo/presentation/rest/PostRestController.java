package com.example.demo.presentation.rest;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.MediaDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
import com.example.demo.enumeration.MediaType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor @Slf4j
public class PostRestController {

    private final PostService postService;
    private final UserService userService;
    private final ContentService contentService;
    private final MediaService mediaService;
    private final TopicService topicService;
    private final MentionService mentionService;
    private final FileUploadService uploadService;
    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<Post>> findAllPosts(){
        List<Post> posts = postService.findAllPosts();

        if(posts != null && !posts.isEmpty()){
            return new ResponseEntity<>(posts,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/create")
    public ResponseEntity<PostDto> createPost(
            @RequestParam("text") String text,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "selectedGroupId", required = false) Long selectedGroupId,
            @RequestParam(name = "gifUrl", required = false) List<String> gifUrls,
            @RequestParam(name = "mentionStaff", required = false) List<String> userList,
            Principal principal) throws IOException, ExecutionException, InterruptedException {
        User user = userService.findByStaffId(principal.getName());
        String newText = topicService.extractTopicWithoutHash(text);
        Content newContent = contentService.createContent(newText);

        if (files != null && !files.isEmpty()) {
            List<CompletableFuture<String>> uploadFutures = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String ext = getFileExtension(file.getOriginalFilename());
                    if (isImageFile(ext)) {
                        uploadFutures.add(handleImageFile(file, newContent));
                    } else {
                        uploadFutures.add(handleOtherFile(file, newContent));
                    }
                }
            }
            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();
        }

        if (gifUrls != null && !gifUrls.isEmpty()) {
            for (String gifUrl : gifUrls) {
                mediaService.createMedia(gifUrl, MediaType.IMAGE, newContent);
            }
        }

        Post newPost = postService.createPost(newContent, text);
        if (selectedGroupId != null && selectedGroupId != 0L) {
            groupService.findById(selectedGroupId).ifPresent(g -> postService.saveGroup(newPost, g));
        }
        if (userList != null && !userList.isEmpty()) {
            for (String staffId : userList) {
                mentionService.saveNewMention(newPost, staffId);
            }
        }
        PostDto dto = postService.findByPostDtoId(newPost.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/details/{postId}")
    public ResponseEntity<PostDto> getPostDetails(@PathVariable("postId") Long postId) {
        PostDto post = postService.findByPostDtoId(postId);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> findPostById(@PathVariable("postId") Long postId){
        PostDto post = postService.findByPostDtoId(postId);

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
    @GetMapping("/findByCurrentUser")
    public ResponseEntity<?> getPosts(){
        List<PostDto> postDtoList = postService.getAllPostByUser();
        log.info("postList {}",postDtoList);
        if(postDtoList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(postDtoList,HttpStatus.OK);
        }
    }

    @GetMapping("/findByUser/{id}")
    public ResponseEntity<?> getPosts(@PathVariable("id")Long userId){
        List<PostDto> postDtoList = postService.findByUserId(userId);
        log.info("postList {}",postDtoList);
        if(postDtoList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(postDtoList,HttpStatus.OK);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updatePost(@RequestParam("exitId") Long id,
                                        @RequestParam("updateText") String text,
                                        @RequestParam(value = "existingImages", required = false) List<String> existingMedia,
                                        @RequestParam(value = "updateFiles", required = false) List<MultipartFile> files,
                                        @RequestParam(value = "removeTag", required = false) List<String> removedTags,
                                        @RequestParam(value = "removeUser", required = false) List<Long> removeUsers,
                                        @RequestParam(value = "mentionStaff", required = false) List<String> userList,
                                        @RequestParam(value = "removedImages", required = false) List<String> removeMedia) throws IOException, ExecutionException, InterruptedException, ExecutionException {

        Post exitPost = postService.findById(id);
        String newText = topicService.extractTopicWithoutHash(text);
        Content exitContent = exitPost.getContent();
        log.info("Image Urls {}",removeMedia);
        log.info("MentionUserList {}", userList);
        log.info("REMOVE TAGS {}", removedTags);
        if (removedTags != null && !removedTags.isEmpty()) {
            topicService.deleteTopicFromPost(removedTags, exitPost.getId());
        }
        if (removeUsers != null && !removeUsers.isEmpty()) {
            mentionService.deleteMentionUser(exitPost.getId(), removeUsers);
        }

        if (userList != null && !userList.isEmpty()) {
            for (String user : userList) {
                log.info("User StaffId {}", user);
                mentionService.saveNewMention(exitPost, user);
                log.info("New Mention User {}", user);
            }
        }
        if (newText != null && !newText.isEmpty()) {
            contentService.updateContent(exitContent.getId(), newText);
        }
        if (removeMedia != null && !removeMedia.isEmpty()) {
            for (String mediaUrl : removeMedia) {
                mediaService.deleteByUrl(mediaUrl, exitContent.getId());
            }
        }

        if (existingMedia != null && !existingMedia.isEmpty()) {
            for (String mediaUrl : existingMedia) {
                MediaType type = mediaUrl.endsWith(".mp4") ? MediaType.VIDEO : MediaType.IMAGE;
                mediaService.createMedia(mediaUrl, type, exitContent);
            }
        }

        if (files != null && !files.isEmpty()) {
            // List to store the image upload futures
            List<CompletableFuture<String>> uploadFutures = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileExtension = getFileExtension(file.getOriginalFilename());
                    if (isImageFile(fileExtension)) {
                        // Handle image file upload and collect futures
                        CompletableFuture<String> imageUrlFuture = handleImageFile(file, exitContent);
                        uploadFutures.add(imageUrlFuture);
                    } else {
                        // Handle other file synchronously
                        CompletableFuture<String> videoUrlFeature = handleOtherFile(file, exitContent);
                        uploadFutures.add(videoUrlFeature);
                    }
                }
            }

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<String> future : uploadFutures) {
                String mediaUrl = future.get();
                MediaType type = mediaUrl.endsWith(".mp4") ? MediaType.VIDEO : MediaType.IMAGE;
                mediaService.createMedia(mediaUrl, type, exitContent);
            }
        }

        Post savedPost = postService.findById(id);
        topicService.extractTopicList(savedPost, text);
        PostDto post = postService.findByPostDtoId(id);
        log.info("New Post After save {}", post.getContent().getMedia().stream().map(MediaDto::getUrl).toList());
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("/postTest")
    public ResponseEntity<?> getPostDto(){
       PostDto postDto = postService.findByPostDtoId(1L);
       return new ResponseEntity<>(postDto,HttpStatus.OK);
    }


    private void deleteImages(Content content) {
        mediaService.findByContentId(content.getId()).forEach(m -> mediaService.deleteById(m.getId()));
    }

    private boolean isImageFile(String fileExtension) {
        return fileExtension != null && (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png"));
    }


    @PostMapping("/delete")
    public void deletePost(@RequestBody LikeDto postDto){
        postService.deletePost(postDto.getPostId());
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    private CompletableFuture<String> handleImageFile(MultipartFile file, Content content) throws IOException {
        return uploadService.imageFileUpload(file.getBytes())
                .thenApply(imageURL -> {
                    mediaService.createMedia(imageURL, MediaType.IMAGE, content);
                    return imageURL;
                })
                .exceptionally(ex -> {
                    // Handle exception here
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to upload image", ex);
                });
    }
    private CompletableFuture<String> handleOtherFile(MultipartFile file, Content content) throws IOException {
        return uploadService.videoFileUpload(file.getBytes())
                .thenApply(videoUrl -> {
                    mediaService.createMedia(videoUrl, MediaType.VIDEO, content);
                    return videoUrl;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to upload video", ex);
                });
    }

    @GetMapping("/groupPost/{id}")
    public ResponseEntity<?> findByGroupId(@PathVariable("id") Long groupId,Principal principal){
        List<PostDto> posts = postService.getPostFromGroup(groupId,principal);
        log.info("HELlo efafd");
        if(posts != null && !posts.isEmpty()){
            return new ResponseEntity<>(posts,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get-trending-posts-inOneWeek")
    public ResponseEntity<List<PostDto>> getTrendingPostsInOneWeek(){
        List<PostDto> trendingPostInOneWeek = postService.getTrendingPostsInOneWeek();
        log.info("Trending Post", trendingPostInOneWeek);
        return ResponseEntity.ok(trendingPostInOneWeek);
    }

    @GetMapping("get-trending-posts-inOneMonth")
    public ResponseEntity<List<PostDto>> getTrendingPostsInOneMonth(){
        List<PostDto> trendingPostInOneWeek = postService.getTrendingPostsInOneMonth();
        return ResponseEntity.ok(trendingPostInOneWeek);
    }
    @GetMapping("get-trending-posts-inOneYear")
    public ResponseEntity<List<PostDto>> getTrendingPostsInOneYear(){
        List<PostDto> trendingPostInOneWeek = postService.getTrendingPostsInOneYear();
        return ResponseEntity.ok(trendingPostInOneWeek);
    }
    @GetMapping("/totalPostsForCurrentDay")
    public long getTotalPostsForCurrentDay() {
        return postService.getTotalPostsForCurrentDay();
    }
    @GetMapping("/totalPostsForCurrentMonth")
    public long getTotalPostsForCurrentMonth() {
        return postService.getTotalPostsForCurrentMonth();
    }
    @GetMapping("/totalPostsForCurrentYear")
    public long getTotalPostsForCurrentYear() {
        return postService.getTotalPostsForCurrentYear();
    }

    @GetMapping("/topPostsInGroup/{id}")
    public ResponseEntity<?> findTopPostsInGroup(@PathVariable("id")Long groupId){
        List<PostDto> postList = postService.findTopPostsInGroup(groupId);

        if(postList != null && !postList.isEmpty()){
            return new ResponseEntity<>(postList,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
