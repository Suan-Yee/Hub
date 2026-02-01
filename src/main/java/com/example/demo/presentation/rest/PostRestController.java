package com.example.demo.presentation.rest;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
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
    private final ImageService imageService;
    private final VideoService videoService;
    private final TopicService topicService;
    private final MentionService mentionService;
    private final FileUploadService uploadService;

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
                if (mediaUrl.endsWith(".mp4")) {
                    videoService.deleteVideoByUrl(mediaUrl, exitContent.getId());
                } else {
                    imageService.deleteImageByUrl(mediaUrl, exitContent.getId());
                }
            }
        }

        List<Image> images = new ArrayList<>();
        List<Video> videos = new ArrayList<>();
        if (existingMedia != null && !existingMedia.isEmpty()) {
            for (String mediaUrl : existingMedia) {
                if (mediaUrl.endsWith(".mp4")) {
                    Video video = new Video();
                    video.setName(mediaUrl);
                    video.setContent(exitContent);
                    videos.add(video);
                } else {
                    Image image = new Image();
                    image.setName(mediaUrl);
                    image.setContent(exitContent);
                    images.add(image);
                }
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

            CompletableFuture<Void> allUploads = CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]));
            allUploads.join();

            // Optionally process the uploaded image URLs if needed
            for (CompletableFuture<String> future : uploadFutures) {
                String mediaUrl = future.get(); // This blocks until the upload completes
                if (mediaUrl.endsWith(".mp4")) {
                    Video video = new Video();
                    video.setName(mediaUrl);
                    video.setContent(exitContent);
                    videos.add(video);
                } else {
                    Image image = new Image();
                    image.setName(mediaUrl);
                    image.setContent(exitContent);
                    images.add(image);
                }
            }
        }

//        exitContent.setImages(images);
//        contentService.save(exitContent);
        Post savedPost = postService.findById(id);
        topicService.extractTopicList(savedPost, text);
        PostDto post = postService.findByPostDtoId(id);
        log.info("New Post After save {}",post.getContent().getImages().stream().map(Image::getName).toList());
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("/postTest")
    public ResponseEntity<?> getPostDto(){
       PostDto postDto = postService.findByPostDtoId(1L);
       return new ResponseEntity<>(postDto,HttpStatus.OK);
    }


    private void deleteImages(Content content) {
        List<Image> images = imageService.findByContentId(content.getId());
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                // Delete image from database
                imageService.deleteImage(image.getId());
                // Optionally, delete image file from storage if applicable
                // uploadService.deleteImageFile(image.getName());
            }
        }
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
        byte[] fileBytes = file.getBytes(); // Read file bytes synchronously
        return uploadService.imageFileUpload(fileBytes)
                .thenApply(imageURL -> {
                    Image newImage = imageService.createImage(imageURL, content);
                    System.out.println(newImage);
                    System.out.println("Handling an image file: " + file.getOriginalFilename());
                    return imageURL;
                })
                .exceptionally(ex -> {
                    // Handle exception here
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to upload image", ex);
                });
    }
    private CompletableFuture<String> handleOtherFile(MultipartFile file, Content content) throws IOException {
        byte[] fileBytes = file.getBytes();
        return uploadService.videoFileUpload(fileBytes)
                .thenApply(videoUrl -> {
                    Video newVideo = videoService.createVideo(videoUrl, content);
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
