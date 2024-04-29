package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/post")
@Controller
@RequiredArgsConstructor
public class PostController {

    private final ContentService contentService;
    private final ImageService imageService;
    private final VideoService videoService;
    private final FileService fileService;
    private final FileUploadService uploadService;
    private final PostService postService;

    @GetMapping("/details/{postId}")
    public String showPost(@PathVariable("postId")Long postId){

        return "/detailsPost";
    }

    @PostMapping("/create")
    public String insertPost(@RequestParam("text")String text, @RequestParam(name = "videos", required = false) List<MultipartFile> videos,
                           @RequestParam(name = "images", required = false) List<MultipartFile> images,
                           @RequestParam(name = "files", required = false)List<MultipartFile> files) throws IOException {
        System.out.println(text);

        Content newContent=contentService.createContent(text);

        if (videos!=null && !videos.isEmpty()){
            for (MultipartFile video : videos){
                if (!video.isEmpty()){
                    String videoURL=uploadService.videoFileUpload(video);
                    System.out.println(videoURL);
                    Video newVideo= videoService.createVideo(videoURL,newContent);
                    System.out.println(newVideo);
                }
            }
        }

        if(images!=null && !images.isEmpty()){
            for (MultipartFile image : images){
                if (!image.isEmpty()){
                    String imageURL=uploadService.imageFileUpload(image);
                    System.out.println(imageURL);
                    Image newImage=imageService.createImage(imageURL,newContent);
                    System.out.println(newImage);
                }
            }
        }

        if(files!=null && !files.isEmpty()){
            for (MultipartFile file : files){
                if (!file.isEmpty()){
                    String fileURL=uploadService.fileUpload(file);
                    System.out.println(fileURL);
                    File newFile=fileService.createFile(fileURL,newContent);
                    System.out.println(newFile);
                }
            }
        }

        Post newPost = postService.createPost(newContent);
        System.out.println(newPost);
        return "redirect:/index";
    }

    @PostMapping("/update/{id}")
    public void updatePost(@PathVariable("id") long id){
        Post exitPost=postService.findById(id);
        Content exitContent=contentService.findById(exitPost.getContent().getId());
        List<Image> exitImage=imageService.findByContentId(exitContent.getId());
        Video exitVideo=videoService.findByContentId(exitContent.getId());
        List<File> exitFile=fileService.findByContentId(exitContent.getId());
    }

    @DeleteMapping("/delete/{id}")
    public void deletePost(@PathVariable("id")long id){
        Post exitPost=postService.findById(id);
        Content exitContent=contentService.findById(exitPost.getContent().getId());
        List<Image> exitImage=imageService.findByContentId(exitContent.getId());
        Video exitVideo=videoService.findByContentId(exitContent.getId());
        List<File> exitFile=fileService.findByContentId(exitContent.getId());
    }
}
