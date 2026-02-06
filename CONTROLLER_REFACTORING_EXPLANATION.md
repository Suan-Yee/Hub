# Controller Refactoring - From @RequestParam to @ModelAttribute

## The Problem with Multiple @RequestParam

### Before (Bad Practice ❌)

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<PostResponse> createPost(
        @RequestParam("content") String content,
        @RequestParam(value = "tags", required = false) List<String> tags,
        @RequestParam(value = "mentions", required = false) List<String> mentions,
        @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
        @RequestParam(value = "pollQuestion", required = false) String pollQuestion,
        @RequestParam(value = "pollOptions", required = false) List<String> pollOptions,
        @RequestParam(value = "groupId", required = false) Long groupId,
        @RequestParam(value = "visibility", required = false, defaultValue = "public") String visibility,
        @AuthenticationPrincipal UserDetails userDetails
) throws IOException {
    // Manual mapping logic...
    CreatePostRequest.PollRequest pollRequest = null;
    if (pollQuestion != null && pollOptions != null && !pollOptions.isEmpty()) {
        pollRequest = new CreatePostRequest.PollRequest(pollQuestion, pollOptions);
    }
    
    CreatePostRequest request = new CreatePostRequest(
        content, tags, mentions, mediaFiles, 
        pollRequest, groupId, visibility
    );
    // ...
}
```

**Issues:**
1. ❌ **Too many parameters** (9 parameters!) - hard to read and maintain
2. ❌ **Verbose** - repetitive `@RequestParam` annotations
3. ❌ **Manual mapping** - converting form data to request object in controller
4. ❌ **No reusability** - same parameters needed for update endpoint
5. ❌ **Validation scattered** - validation logic mixed with mapping logic
6. ❌ **Hard to test** - need to pass many arguments in tests

---

## The Solution with @ModelAttribute

### After (Best Practice ✅)

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<PostResponse> createPost(
        @Valid @ModelAttribute CreatePostForm form,
        @AuthenticationPrincipal UserDetails userDetails
) throws IOException {
    Long userId = extractUserId(userDetails);
    CreatePostRequest request = form.toRequest();
    PostResponse response = postService.createPost(request, userId);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Benefits:**
1. ✅ **Clean and concise** - only 2 parameters
2. ✅ **Reusable** - `CreatePostForm` can be used in multiple endpoints
3. ✅ **Validation in DTO** - all validation annotations in one place
4. ✅ **Easy to test** - just create a form object
5. ✅ **Separation of concerns** - form binding separate from business logic
6. ✅ **Type-safe** - compile-time checking of form fields

---

## The Form DTO

```java
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
            content, tags, mentions, mediaFiles,
            pollRequest, groupId, visibility
        );
    }
}
```

**Key Points:**
- Uses `@Data` (Lombok) for getters/setters - required for Spring form binding
- Validation annotations (`@NotBlank`, `@Size`) in one place
- `toRequest()` method encapsulates conversion logic
- Mutable fields (not a Record) because Spring needs setters for binding

---

## Why Not Use Record for Form Binding?

### Records Don't Work Well with @ModelAttribute ❌

```java
// This WON'T work properly with multipart/form-data
public record CreatePostForm(
    String content,
    List<String> tags,
    // ...
) {}
```

**Why?**
1. Records are **immutable** - no setters
2. Spring's form binding requires **setters** to populate fields
3. Multipart/form-data binding happens **incrementally** as data arrives
4. Records work better for **JSON** requests, not form data

### When to Use Record vs Class

| Use Case | Use Record | Use Class (@Data) |
|----------|-----------|-------------------|
| JSON Request Body | ✅ Yes | ❌ No |
| JSON Response | ✅ Yes | ❌ No |
| Form Data (multipart) | ❌ No | ✅ Yes |
| Query Parameters (many) | ❌ No | ✅ Yes |

---

## Comparison: Lines of Code

### Before
```java
// Controller method: 35 lines
// No separate form class
// Total: 35 lines
```

### After
```java
// Controller method: 8 lines
// Form class: 40 lines (reusable!)
// Total: 48 lines, but much more maintainable
```

**The extra lines are worth it because:**
- Form class is **reusable** across multiple endpoints
- **Validation** is centralized
- **Easier to test** and maintain
- **Cleaner** controller code

---

## How Spring Binds the Form

### Request Flow

```
1. Client sends multipart/form-data:
   POST /api/posts
   Content-Type: multipart/form-data
   
   content: "Hello World"
   tags: "test"
   tags: "hello"
   mediaFiles: [file1.jpg]
   visibility: "public"

2. Spring creates CreatePostForm instance

3. Spring calls setters for each field:
   form.setContent("Hello World")
   form.setTags(List.of("test", "hello"))
   form.setMediaFiles(List.of(file1))
   form.setVisibility("public")

4. Spring validates with @Valid

5. Controller receives populated form object
```

---

## Testing Comparison

### Before (Verbose ❌)

```java
@Test
void testCreatePost() {
    mockMvc.perform(multipart("/api/posts")
        .param("content", "Test")
        .param("tags", "tag1")
        .param("tags", "tag2")
        .param("mentions", "user1")
        .param("pollQuestion", "Question?")
        .param("pollOptions", "Option 1")
        .param("pollOptions", "Option 2")
        .param("groupId", "1")
        .param("visibility", "public")
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated());
}
```

### After (Clean ✅)

```java
@Test
void testCreatePost() {
    CreatePostForm form = new CreatePostForm();
    form.setContent("Test");
    form.setTags(List.of("tag1", "tag2"));
    form.setMentions(List.of("user1"));
    form.setVisibility("public");
    
    mockMvc.perform(multipart("/api/posts")
        .flashAttr("createPostForm", form)
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated());
}
```

---

## Alternative Approaches

### 1. Using @RequestBody with JSON (Not Suitable for File Upload)

```java
// This works for JSON but NOT for file uploads
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody CreatePostRequest request,
        @AuthenticationPrincipal UserDetails userDetails
) {
    // ...
}
```

**Problem:** Can't send files in JSON body

### 2. Separate Endpoints (Over-engineered)

```java
// Too many endpoints!
@PostMapping("/text")
public ResponseEntity<PostResponse> createTextPost(...)

@PostMapping("/media")
public ResponseEntity<PostResponse> createMediaPost(...)

@PostMapping("/poll")
public ResponseEntity<PostResponse> createPollPost(...)
```

**Problem:** Violates DRY principle, too complex

### 3. Using Map<String, Object> (Type-unsafe ❌)

```java
@PostMapping
public ResponseEntity<PostResponse> createPost(
        @RequestParam Map<String, Object> params
) {
    // Manual type casting, no validation
    String content = (String) params.get("content");
    // ...
}
```

**Problem:** No type safety, no validation, error-prone

---

## Best Practices Summary

### ✅ DO

1. **Use @ModelAttribute** for multipart/form-data with many fields
2. **Create separate Form DTOs** for form binding
3. **Use @Data (Lombok)** for form classes (need setters)
4. **Add validation annotations** to form fields
5. **Encapsulate conversion logic** in `toRequest()` method
6. **Keep controllers thin** - delegate to services

### ❌ DON'T

1. **Don't use many @RequestParam** - creates verbose code
2. **Don't use Records for form binding** - they're immutable
3. **Don't mix validation and mapping** in controllers
4. **Don't repeat form parameters** across endpoints
5. **Don't use Map<String, Object>** - lose type safety

---

## Migration Guide

If you have existing endpoints with many `@RequestParam`:

### Step 1: Create Form DTO
```java
@Data
public class YourForm {
    // Add fields with validation
}
```

### Step 2: Update Controller
```java
// Before
public ResponseEntity<?> endpoint(
    @RequestParam String field1,
    @RequestParam String field2,
    // ...
) { }

// After
public ResponseEntity<?> endpoint(
    @Valid @ModelAttribute YourForm form
) { }
```

### Step 3: Test
- Verify form binding works
- Check validation
- Test file uploads

---

## Conclusion

The refactored approach using `@ModelAttribute` with a dedicated Form DTO is:

- ✅ **More maintainable** - centralized form structure
- ✅ **More testable** - easy to create form objects
- ✅ **More reusable** - same form for create/update
- ✅ **More readable** - clean controller methods
- ✅ **More type-safe** - compile-time checking
- ✅ **Better validation** - annotations in one place

This is the **Spring Boot best practice** for handling complex form submissions with file uploads.
