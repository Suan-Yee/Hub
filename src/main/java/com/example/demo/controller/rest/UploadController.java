package com.example.demo.controller.rest;

import com.example.demo.domain.ApiResponse;
import com.example.demo.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) throws IOException {
        String contentType = file.getContentType();
        String url;
        String type;

        if (contentType != null && contentType.startsWith("image/")) {
            url = fileUploadService.uploadImage(file);
            type = "image";
        } else if (contentType != null && contentType.startsWith("video/")) {
            url = fileUploadService.uploadFile(file);
            type = "video";
        } else {
            url = fileUploadService.uploadFile(file);
            type = "file";
        }

        ApiResponse apiResponse = ApiResponse.created(
                request.getRequestURI(),
                "File uploaded successfully",
                Map.of(
                        "url", url,
                        "type", type
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
