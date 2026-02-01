package com.example.demo.presentation.rest;

import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequestMapping("/post")
@Controller
@RequiredArgsConstructor @Slf4j
public class PostController {

    private final ContentService contentService;
    private final ImageService imageService;
    private final VideoService videoService;
    private final FileService fileService;
    private final FileUploadService uploadService;
    private final PostService postService;
    private final GroupService groupService;
    private final MentionService mentionService;
    private final TopicService topicService;

    @GetMapping("/details/{postId}")
    public String showPost(@PathVariable("postId")Long postId){

        return "/detailsPost";
    }

    @PostMapping("/create")
    public String insertPost(
            RedirectAttributes redirectAttributes,
            @RequestParam("text") String text,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "selectedGroupId") Long selectedGroupId,
            @RequestParam(name = "gifUrl", required = false) List<String> gifUrls,
            @RequestParam(name = "mentionStaff",required = false) List<String> userList) throws IOException, InterruptedException, ExecutionException {


        String newText = topicService.extractTopicWithoutHash(text);
        Content newContent = contentService.createContent(newText);

        if (files != null && !files.isEmpty()) {
            List<CompletableFuture<String>> uploadFutures = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
                    if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                        CompletableFuture<String> imageUrlFuture = handleImageFile(file, newContent);
                        uploadFutures.add(imageUrlFuture);
                    } else {
                        CompletableFuture<String> videoUrlFuture = handleOtherFile(file, newContent);
                        uploadFutures.add(videoUrlFuture);
                    }
                }
            }
            CompletableFuture<Void> allUploads = CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]));
            allUploads.join();

            for (CompletableFuture<String> future : uploadFutures) {
                String fileUrl = future.get();
                // Do something with fileUrl if needed
            }
        }

        if (gifUrls != null && !gifUrls.isEmpty()) {
            for (String gifUrl : gifUrls) {
                Image newImage = imageService.createImage(gifUrl, newContent);
                System.out.println("Handling a GIF URL: " + gifUrl);
            }
        }

        Post newPost = postService.createPost(newContent,text);
        if (selectedGroupId != null && selectedGroupId != 0L) {
            Group group = groupService.findById(selectedGroupId).orElse(null);
            postService.saveGroup(newPost, group);
        }
        if (userList != null && !userList.isEmpty()) {
            for (String user : userList) {
                log.info("User StaffId {}", user);
                mentionService.saveNewMention(newPost, user);
            }
        }
        redirectAttributes.addFlashAttribute("PostSuccess", true);
        return "redirect:/index";
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
        byte[] fileBytes = file.getBytes();
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

}
