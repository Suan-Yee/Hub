package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Form DTO for creating posts with multipart/form-data
 * Uses @Data for mutable fields (required for form binding)
 */
@Data
public class CreatePostForm {
    
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;
    
    private List<String> tags;
    
    private List<String> mentions;
    
    private List<MultipartFile> mediaFiles;
    
    private String pollQuestion;
    
    @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
    private List<String> pollOptions;
    
    private Long groupId;
    
    private String visibility = "public";
    
    /**
     * Convert to CreatePostRequest
     */
    public CreatePostRequest toRequest() {
        CreatePostRequest.PollRequest pollRequest = null;
        if (pollQuestion != null && pollOptions != null && !pollOptions.isEmpty()) {
            pollRequest = new CreatePostRequest.PollRequest(pollQuestion, pollOptions);
        }
        
        return new CreatePostRequest(
            content,
            tags,
            mentions,
            mediaFiles,
            pollRequest,
            groupId,
            visibility
        );
    }
}
