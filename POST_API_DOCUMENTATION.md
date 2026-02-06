# Post Service API Documentation

## Overview
This document describes the Post Service API for the social media platform. The API provides endpoints for creating, reading, updating, and deleting posts, as well as managing reactions and bookmarks.

## Table of Contents
- [Post Entity Structure](#post-entity-structure)
- [API Endpoints](#api-endpoints)
  - [Post Management](#post-management)
  - [Reactions](#reactions)
  - [Bookmarks](#bookmarks)
- [Request/Response Examples](#requestresponse-examples)

## Post Entity Structure

### Post Response
```json
{
  "id": "string",
  "author": "string",
  "authorId": "string",
  "handle": "string",
  "avatar": "string",
  "time": "string",
  "content": "string",
  "tags": ["string"],
  "mentions": ["string"],
  "media": [
    {
      "id": "string",
      "type": "image|video",
      "url": "string"
    }
  ],
  "poll": {
    "question": "string",
    "options": [
      {
        "id": "string",
        "label": "string",
        "votes": 0,
        "voted": false
      }
    ],
    "totalVotes": 0
  },
  "comments": [],
  "likes": 0,
  "liked": false,
  "bookmarked": false,
  "group": "string",
  "edited": false,
  "editedAt": "string",
  "resharedFrom": null,
  "reactions": {
    "LIKE": 0,
    "LOVE": 0,
    "HAHA": 0,
    "SAD": 0,
    "ANGRY": 0
  },
  "userReaction": "LIKE|LOVE|HAHA|SAD|ANGRY"
}
```

## API Endpoints

### Post Management

#### Create Post
**POST** `/api/posts`

Creates a new post with optional media, poll, and group association.

**Content-Type:** `multipart/form-data`

**Parameters:**
- `content` (required): Post content text
- `tags` (optional): Array of hashtags
- `mentions` (optional): Array of mentioned usernames
- `mediaFiles` (optional): Array of media files (images/videos)
- `pollQuestion` (optional): Poll question text
- `pollOptions` (optional): Array of poll option texts
- `groupId` (optional): Group ID to post in
- `visibility` (optional): Post visibility (`public`, `followers`, `me`) - default: `public`

**Response:** `201 Created`
```json
{
  "id": "1",
  "author": "johndoe",
  "content": "Hello World!",
  ...
}
```

---

#### Get Post by ID
**GET** `/api/posts/{postId}`

Retrieves a single post by its ID.

**Response:** `200 OK`
```json
{
  "id": "1",
  "author": "johndoe",
  ...
}
```

---

#### Get Posts by User
**GET** `/api/posts/user/{userId}`

Retrieves all posts created by a specific user.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
{
  "content": [...],
  "pageable": {...},
  "totalPages": 5,
  "totalElements": 100
}
```

---

#### Get Public Feed
**GET** `/api/posts/feed`

Retrieves public posts for the feed.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
{
  "content": [...],
  "pageable": {...}
}
```

---

#### Get Posts by Group
**GET** `/api/posts/group/{groupId}`

Retrieves all posts in a specific group.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`

---

#### Update Post
**PUT** `/api/posts/{postId}`

Updates an existing post. Only the post author can update.

**Content-Type:** `multipart/form-data`

**Parameters:**
- `content` (required): Updated post content
- `tags` (optional): Updated hashtags
- `mentions` (optional): Updated mentions
- `mediaFiles` (optional): New media files (replaces old ones)
- `visibility` (optional): Updated visibility

**Response:** `200 OK`

---

#### Delete Post
**DELETE** `/api/posts/{postId}`

Deletes a post. Only the post author can delete.

**Response:** `200 OK`
```json
{
  "message": "Post deleted successfully"
}
```

---

### Reactions

#### Add/Update Reaction to Post
**POST** `/api/reactions/post/{postId}`

Adds or updates a reaction to a post.

**Query Parameters:**
- `reactionType` (required): Reaction type (`LIKE`, `LOVE`, `HAHA`, `SAD`, `ANGRY`)

**Response:** `200 OK`
```json
{
  "message": "Reaction added successfully"
}
```

---

#### Remove Reaction from Post
**DELETE** `/api/reactions/post/{postId}`

Removes the user's reaction from a post.

**Response:** `200 OK`
```json
{
  "message": "Reaction removed successfully"
}
```

---

#### Add/Update Reaction to Comment
**POST** `/api/reactions/comment/{commentId}`

Adds or updates a reaction to a comment.

**Query Parameters:**
- `reactionType` (required): Reaction type

**Response:** `200 OK`

---

#### Remove Reaction from Comment
**DELETE** `/api/reactions/comment/{commentId}`

Removes the user's reaction from a comment.

**Response:** `200 OK`

---

### Bookmarks

#### Add Bookmark
**POST** `/api/bookmarks/post/{postId}`

Bookmarks a post for the current user.

**Response:** `200 OK`
```json
{
  "message": "Post bookmarked successfully"
}
```

---

#### Remove Bookmark
**DELETE** `/api/bookmarks/post/{postId}`

Removes a bookmark from a post.

**Response:** `200 OK`
```json
{
  "message": "Bookmark removed successfully"
}
```

---

#### Get User Bookmarks
**GET** `/api/bookmarks`

Retrieves all bookmarked posts for the current user.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`

---

#### Get Bookmarks by User ID
**GET** `/api/bookmarks/user/{userId}`

Retrieves bookmarked posts for a specific user.

**Query Parameters:**
- `page` (optional): Page number
- `size` (optional): Page size

**Response:** `200 OK`

---

#### Check if Post is Bookmarked
**GET** `/api/bookmarks/post/{postId}/check`

Checks if the current user has bookmarked a post.

**Response:** `200 OK`
```json
{
  "bookmarked": true
}
```

---

## Request/Response Examples

### Example 1: Create a Text Post
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer {token}" \
  -F "content=Hello World! This is my first post" \
  -F "tags=introduction" \
  -F "tags=hello" \
  -F "visibility=public"
```

### Example 2: Create a Post with Media
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer {token}" \
  -F "content=Check out this amazing photo!" \
  -F "mediaFiles=@/path/to/image.jpg" \
  -F "tags=photography"
```

### Example 3: Create a Poll Post
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer {token}" \
  -F "content=What's your favorite programming language?" \
  -F "pollQuestion=Choose your favorite" \
  -F "pollOptions=Java" \
  -F "pollOptions=Python" \
  -F "pollOptions=JavaScript" \
  -F "pollOptions=Go"
```

### Example 4: Add a Reaction
```bash
curl -X POST http://localhost:8080/api/reactions/post/1?reactionType=LOVE \
  -H "Authorization: Bearer {token}"
```

### Example 5: Bookmark a Post
```bash
curl -X POST http://localhost:8080/api/bookmarks/post/1 \
  -H "Authorization: Bearer {token}"
```

### Example 6: Get Public Feed
```bash
curl -X GET "http://localhost:8080/api/posts/feed?page=0&size=20" \
  -H "Authorization: Bearer {token}"
```

---

## Database Schema Updates

### Post Entity Changes
The Post entity has been updated with the following changes:

1. **Renamed Fields:**
   - `caption` → `content`
   - `mediaUrls` → `mediaItems` (now includes type and id)
   - `hashtags` → `tags`

2. **New Fields:**
   - `mentions`: List of mentioned usernames
   - `edited`: Boolean flag indicating if post was edited
   - `pollQuestion`: Question text for poll posts
   - `mediaItems`: Structured list of media items with id, type, and url

3. **MediaItem Structure:**
   ```java
   public static class MediaItem {
       private String id;
       private String type; // "image" or "video"
       private String url;
   }
   ```

### Migration Notes
If you have existing data, you may need to run a migration script to:
1. Rename `caption` to `content`
2. Convert `media_urls` array to `media_items` JSONB structure
3. Convert `hashtags` array to `tags` JSONB structure
4. Add `mentions` JSONB column
5. Add `edited` boolean column (default: false)
6. Add `poll_question` text column

---

## Error Handling

All endpoints return standard HTTP status codes:

- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error Response Format:
```json
{
  "timestamp": "2026-02-06T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Content is required",
  "path": "/api/posts"
}
```

---

## Authentication

All endpoints require authentication via JWT token in the Authorization header:
```
Authorization: Bearer {your-jwt-token}
```

The user ID is automatically extracted from the authenticated user's session.

---

## Features Implemented

✅ Create posts with text, media, and polls
✅ Update and delete posts
✅ Get posts by user, group, or public feed
✅ Reaction system (like, love, haha, sad, angry)
✅ Bookmark system
✅ Media upload support (images and videos via Cloudinary)
✅ Poll creation and voting structure
✅ Mention and hashtag support
✅ Post visibility control
✅ Edit tracking
✅ Pagination support
✅ Reshare/repost support

---

## TODO / Future Enhancements

- [ ] Implement comment mapping in PostResponse
- [ ] Add poll voting endpoints
- [ ] Implement reshare/repost functionality
- [ ] Add search functionality for posts
- [ ] Implement real-time notifications for reactions
- [ ] Add post analytics (views, engagement rate)
- [ ] Implement content moderation
- [ ] Add support for post scheduling
- [ ] Implement draft posts
- [ ] Add support for post templates

---

## Notes

1. **UserDetails Integration**: The `extractUserId()` method in controllers is a placeholder. You need to implement it based on your custom UserDetails implementation.

2. **Media Upload**: The service uses Cloudinary for media storage. Ensure your Cloudinary configuration is properly set up in `CloudinaryConfig.java`.

3. **Pagination**: All list endpoints support pagination with default values of page=0 and size=20.

4. **Reactions**: When a user changes their reaction, the old reaction is automatically replaced with the new one.

5. **Bookmarks**: Users can only bookmark posts once. Attempting to bookmark the same post twice will result in an error.
