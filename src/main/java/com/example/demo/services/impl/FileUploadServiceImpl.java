package com.example.demo.services.impl;

import com.cloudinary.Cloudinary;
import com.example.demo.entity.User;
import com.example.demo.services.FileUploadService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    private final Cloudinary cloudinary;
    private final UserService userService;



    public String extractPublicId(String imgUrl){
        int startIndex = imgUrl.lastIndexOf("/")+1;
        int endIndex = imgUrl.lastIndexOf(".");
        return imgUrl.substring(startIndex,endIndex);
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(user.getPhoto()!=null && !user.getPhoto().isEmpty()){
            deleteImage(user.getPhoto());
        }
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()))
                .get("url")
                .toString();
    }

    @Override
    public Boolean deleteImage(String imgUrl) throws IOException {
        String publicId = extractPublicId(imgUrl);
        cloudinary.uploader().destroy(publicId,null);
        return true;
    }
    @Override
    public String videoFileUpload(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader().uploadLarge(multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString(), "resource_type", "video"))
                .get("url").toString();
    }

    @Override
    public String imageFileUpload(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader().upload(multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
    }

    @Override
    public String fileUpload(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader().upload(multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
    }

}
