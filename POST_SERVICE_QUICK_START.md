# Post Service Quick Start Guide

## Prerequisites

1. Java 21 installed
2. PostgreSQL database running
3. Cloudinary account configured
4. Maven installed

## Configuration

### 1. Database Setup

Create the database and run migrations:

```sql
-- Create database
CREATE DATABASE social_hub;

-- Connect to database
\c social_hub

-- The tables should already exist from your schema
-- But you need to add the new columns to the posts table:

ALTER TABLE posts RENAME COLUMN caption TO content;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS mentions JSONB;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS edited BOOLEAN DEFAULT FALSE;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS poll_question TEXT;

-- If you need to convert hashtags to tags:
ALTER TABLE posts RENAME COLUMN hashtags TO tags;
```

### 2. Application Properties

Ensure your `application.properties` or `application.yml` has:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/social_hub
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Cloudinary (from your existing config)
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Running the Application

```bash
# Navigate to project directory
cd c:\SpringBoot\Hub

# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The application should start on `http://localhost:8080`

## Testing the API

### Using cURL

#### 1. Create a Simple Text Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "content=Hello World! This is my first post #test" \
  -F "tags=test" \
  -F "tags=hello" \
  -F "visibility=public"
```

#### 2. Create a Post with Image

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "content=Check out this amazing photo!" \
  -F "mediaFiles=@C:\path\to\image.jpg" \
  -F "tags=photography"
```

#### 3. Create a Poll Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "content=What's your favorite programming language?" \
  -F "pollQuestion=Choose your favorite" \
  -F "pollOptions=Java" \
  -F "pollOptions=Python" \
  -F "pollOptions=JavaScript" \
  -F "pollOptions=TypeScript"
```

#### 4. Get Public Feed

```bash
curl -X GET "http://localhost:8080/api/posts/feed?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Add a Reaction

```bash
curl -X POST "http://localhost:8080/api/reactions/post/1?reactionType=LOVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 6. Bookmark a Post

```bash
curl -X POST http://localhost:8080/api/bookmarks/post/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 7. Get Bookmarks

```bash
curl -X GET "http://localhost:8080/api/bookmarks?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

#### 1. Setup Environment

Create a new environment with:
- `baseUrl`: `http://localhost:8080`
- `token`: Your JWT token

#### 2. Create Post Request

- Method: `POST`
- URL: `{{baseUrl}}/api/posts`
- Headers:
  - `Authorization`: `Bearer {{token}}`
- Body (form-data):
  - `content`: "Hello World!"
  - `tags`: ["test", "hello"]
  - `visibility`: "public"
  - `mediaFiles`: (select file)

#### 3. Get Feed Request

- Method: `GET`
- URL: `{{baseUrl}}/api/posts/feed?page=0&size=10`
- Headers:
  - `Authorization`: `Bearer {{token}}`

### Using JavaScript/Fetch

```javascript
// Create a post
async function createPost() {
  const formData = new FormData();
  formData.append('content', 'Hello from JavaScript!');
  formData.append('tags', 'javascript');
  formData.append('tags', 'test');
  formData.append('visibility', 'public');
  
  const response = await fetch('http://localhost:8080/api/posts', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${yourToken}`
    },
    body: formData
  });
  
  const post = await response.json();
  console.log('Created post:', post);
}

// Get feed
async function getFeed() {
  const response = await fetch('http://localhost:8080/api/posts/feed?page=0&size=10', {
    headers: {
      'Authorization': `Bearer ${yourToken}`
    }
  });
  
  const feed = await response.json();
  console.log('Feed:', feed);
}

// Add reaction
async function addReaction(postId, reactionType) {
  const response = await fetch(
    `http://localhost:8080/api/reactions/post/${postId}?reactionType=${reactionType}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${yourToken}`
      }
    }
  );
  
  const result = await response.json();
  console.log('Reaction added:', result);
}

// Bookmark post
async function bookmarkPost(postId) {
  const response = await fetch(
    `http://localhost:8080/api/bookmarks/post/${postId}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${yourToken}`
      }
    }
  );
  
  const result = await response.json();
  console.log('Bookmarked:', result);
}
```

## Important Notes

### 1. Authentication Setup

The controllers have a placeholder `extractUserId()` method. You need to implement it:

```java
// In PostController.java, ReactionController.java, BookmarkController.java
private Long extractUserId(UserDetails userDetails) {
    if (userDetails == null) {
        throw new RuntimeException("User not authenticated");
    }
    
    // Option 1: If you have a custom UserDetails implementation
    if (userDetails instanceof CustomUserDetails) {
        return ((CustomUserDetails) userDetails).getId();
    }
    
    // Option 2: If you store user ID in username
    return Long.parseLong(userDetails.getUsername());
    
    // Option 3: Query from database
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
}
```

### 2. CORS Configuration

If you're testing from a frontend on a different port, add CORS configuration:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### 3. Security Configuration

Ensure your SecurityConfig allows the post endpoints:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/posts/**").authenticated()
            .requestMatchers("/api/reactions/**").authenticated()
            .requestMatchers("/api/bookmarks/**").authenticated()
            // ... other matchers
        )
        // ... rest of config
    return http.build();
}
```

## Testing Checklist

- [ ] Create a text post
- [ ] Create a post with image
- [ ] Create a post with video
- [ ] Create a poll post
- [ ] Get post by ID
- [ ] Get user's posts
- [ ] Get public feed
- [ ] Get group posts
- [ ] Update a post
- [ ] Delete a post
- [ ] Add LIKE reaction
- [ ] Add LOVE reaction
- [ ] Change reaction type
- [ ] Remove reaction
- [ ] Bookmark a post
- [ ] Remove bookmark
- [ ] Get bookmarked posts
- [ ] Check bookmark status

## Common Issues and Solutions

### Issue 1: File Upload Fails
**Solution**: Check `spring.servlet.multipart.max-file-size` in properties

### Issue 2: 401 Unauthorized
**Solution**: Ensure JWT token is valid and properly formatted in Authorization header

### Issue 3: Media URLs are null
**Solution**: Verify Cloudinary configuration and credentials

### Issue 4: User ID is always 1
**Solution**: Implement proper `extractUserId()` method based on your authentication

### Issue 5: Database errors on startup
**Solution**: Run the migration SQL commands to update the posts table schema

## Sample Data

Here's a SQL script to insert some test data:

```sql
-- Insert test user
INSERT INTO users (username, email, password_hash, bio, avatar_url, is_verified, created_at)
VALUES ('testuser', 'test@example.com', 'hashed_password', 'Test user bio', 
        'https://via.placeholder.com/150', false, NOW());

-- Insert test posts
INSERT INTO posts (user_id, type, content, tags, mentions, visibility, 
                   likes_count, comments_count, shares_count, edited, created_at, updated_at)
VALUES 
  (1, 'text', 'Hello World! This is a test post', '["test", "hello"]'::jsonb, 
   '[]'::jsonb, 'public', 0, 0, 0, false, NOW(), NOW()),
  (1, 'text', 'Another test post with mentions', '["test"]'::jsonb, 
   '["@johndoe"]'::jsonb, 'public', 0, 0, 0, false, NOW(), NOW());
```

## Monitoring and Logs

Enable detailed logging in `application.properties`:

```properties
# Logging
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

Watch the logs for:
- SQL queries
- Service method calls
- File upload progress
- Error messages

## Next Steps

1. Implement proper authentication integration
2. Add validation error handling
3. Implement poll voting endpoints
4. Add comment creation endpoints
5. Set up automated tests
6. Configure production database
7. Set up CI/CD pipeline

## Support

For issues or questions:
1. Check the logs for error messages
2. Review the API documentation (POST_API_DOCUMENTATION.md)
3. Check the implementation summary (POST_SERVICE_IMPLEMENTATION_SUMMARY.md)
4. Verify database schema is up to date

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Cloudinary Java SDK](https://cloudinary.com/documentation/java_integration)
- [PostgreSQL JSONB](https://www.postgresql.org/docs/current/datatype-json.html)
- [JWT Authentication](https://jwt.io/)
