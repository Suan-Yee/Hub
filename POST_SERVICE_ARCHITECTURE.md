# Post Service Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (React/TypeScript)              │
│                                                                  │
│  Components: PostCard, CreatePost, Feed, Reactions, Bookmarks   │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST API
                             │ (JSON + multipart/form-data)
┌────────────────────────────▼────────────────────────────────────┐
│                      Controller Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │PostController│  │ReactionCtrl  │  │BookmarkCtrl  │          │
│  │              │  │              │  │              │          │
│  │ 7 endpoints  │  │ 4 endpoints  │  │ 5 endpoints  │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          │ Dependency Injection (Constructor)  │
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────────┐
│                       Service Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │PostService   │  │ReactionSvc   │  │BookmarkSvc   │          │
│  │Impl          │  │Impl          │  │Impl          │          │
│  │              │  │              │  │              │          │
│  │Business Logic│  │Business Logic│  │Business Logic│          │
│  │Validation    │  │Validation    │  │Validation    │          │
│  │Authorization │  │Authorization │  │Authorization │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          │ JPA/Hibernate    │                  │
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────────┐
│                    Repository Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │PostRepo      │  │ReactionRepo  │  │BookmarkRepo  │          │
│  │              │  │              │  │              │          │
│  │UserRepo      │  │CommentRepo   │  │PollRepo      │          │
│  │              │  │              │  │              │          │
│  │GroupRepo     │  │              │  │              │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          │ SQL Queries      │                  │
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────────┐
│                    PostgreSQL Database                           │
│                                                                  │
│  Tables: posts, users, reactions, bookmarks, poll_options,      │
│          comments, groups, group_members                         │
│                                                                  │
│  Features: JSONB columns, Indexes, Foreign Keys, Constraints    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                    External Services                             │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Cloudinary                            │   │
│  │                                                          │   │
│  │  - Image Upload/Storage                                 │   │
│  │  - Video Upload/Storage                                 │   │
│  │  - Media Transformation                                 │   │
│  │  - CDN Delivery                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

## Component Interaction Flow

### 1. Create Post Flow

```
User (Frontend)
    │
    │ 1. Submit form with content, media, tags
    │
    ▼
PostController.createPost()
    │
    │ 2. Extract user ID from JWT token
    │ 3. Validate request data
    │
    ▼
PostServiceImpl.createPost()
    │
    │ 4. Verify user exists
    │ 5. Upload media files to Cloudinary
    │ 6. Create Post entity
    │ 7. Create PollOptions if poll
    │ 8. Save to database
    │
    ▼
PostRepository.save()
    │
    │ 9. Execute SQL INSERT
    │
    ▼
PostgreSQL Database
    │
    │ 10. Return saved entity
    │
    ▼
PostServiceImpl.mapToPostResponse()
    │
    │ 11. Map entity to DTO
    │ 12. Get reaction counts
    │ 13. Check bookmark status
    │ 14. Format timestamps
    │
    ▼
PostController
    │
    │ 15. Return HTTP 201 with PostResponse
    │
    ▼
User (Frontend)
```

### 2. Add Reaction Flow

```
User (Frontend)
    │
    │ 1. Click reaction button (e.g., LOVE)
    │
    ▼
ReactionController.addReactionToPost()
    │
    │ 2. Extract user ID from JWT
    │ 3. Validate reaction type
    │
    ▼
ReactionServiceImpl.addReactionToPost()
    │
    │ 4. Verify post exists
    │ 5. Verify user exists
    │ 6. Check for existing reaction
    │
    ├─── If existing reaction:
    │    │ 7a. Update reaction type
    │    └─▶ Save updated reaction
    │
    └─── If no existing reaction:
         │ 7b. Create new Reaction entity
         │ 8b. Increment post likes_count
         └─▶ Save new reaction
    │
    ▼
ReactionRepository.save()
    │
    │ 9. Execute SQL INSERT/UPDATE
    │
    ▼
PostgreSQL Database
    │
    │ 10. Return success
    │
    ▼
User (Frontend)
```

### 3. Get Feed Flow

```
User (Frontend)
    │
    │ 1. Request feed (page=0, size=20)
    │
    ▼
PostController.getPublicFeed()
    │
    │ 2. Extract user ID from JWT
    │ 3. Create Pageable object
    │
    ▼
PostServiceImpl.getPublicFeed()
    │
    │ 4. Query public posts
    │
    ▼
PostRepository.findPublicPosts()
    │
    │ 5. Execute SQL SELECT with pagination
    │
    ▼
PostgreSQL Database
    │
    │ 6. Return Page<Post>
    │
    ▼
PostServiceImpl
    │
    │ 7. For each post:
    │    - Get reactions
    │    - Check if user liked
    │    - Check if user bookmarked
    │    - Get poll options
    │    - Map to PostResponse
    │
    ▼
PostController
    │
    │ 8. Return HTTP 200 with Page<PostResponse>
    │
    ▼
User (Frontend)
```

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          Request Flow                            │
└─────────────────────────────────────────────────────────────────┘

HTTP Request (JSON/FormData)
    │
    ▼
┌───────────────────┐
│   Controller      │  - Route mapping
│                   │  - Authentication check
│                   │  - Request validation
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│   Service         │  - Business logic
│                   │  - Authorization
│                   │  - Data transformation
│                   │  - External service calls
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│   Repository      │  - Database queries
│                   │  - Entity mapping
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│   Database        │  - Data persistence
│                   │  - Constraints
│                   │  - Indexes
└───────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Response Flow                            │
└─────────────────────────────────────────────────────────────────┘

Database Result
    │
    ▼
Repository (Entity)
    │
    ▼
Service (DTO Mapping)
    │
    ▼
Controller (HTTP Response)
    │
    ▼
Frontend (JSON)
```

## Entity Relationship Diagram

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│    User     │         │    Post     │         │   Group     │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │◄───────┤ user_id (FK)│         │ id (PK)     │
│ username    │ 1    * │ group_id(FK)├────────►│ name        │
│ email       │         │ content     │ *    1  │ owner_id    │
│ avatar_url  │         │ type        │         │ privacy     │
│ ...         │         │ tags        │         │ ...         │
└─────────────┘         │ mentions    │         └─────────────┘
                        │ media_items │
      │                 │ visibility  │
      │                 │ edited      │
      │                 │ poll_quest. │
      │                 │ likes_count │
      │                 │ ...         │
      │                 └─────────────┘
      │                       │
      │                       │ 1
      │                       │
      │                       │ *
      │                 ┌─────────────┐
      │                 │ PollOption  │
      │                 ├─────────────┤
      │                 │ id (PK)     │
      │                 │ post_id(FK) │
      │                 │ option_text │
      │                 │ vote_count  │
      │                 └─────────────┘
      │
      │ 1               ┌─────────────┐
      ├────────────────►│  Reaction   │
      │              *  ├─────────────┤
      │                 │ id (PK)     │
      │                 │ user_id(FK) │
      │                 │ target_type │
      │                 │ target_id   │
      │                 │ reaction_ty.│
      │                 └─────────────┘
      │
      │ 1               ┌─────────────┐
      ├────────────────►│  Bookmark   │
      │              *  ├─────────────┤
      │                 │ user_id(FK) │
      │                 │ post_id(FK) │
      │                 │ created_at  │
      │                 └─────────────┘
      │
      │ 1               ┌─────────────┐
      └────────────────►│  Comment    │
                     *  ├─────────────┤
                        │ id (PK)     │
                        │ post_id(FK) │
                        │ user_id(FK) │
                        │ parent_id   │
                        │ content     │
                        │ depth       │
                        └─────────────┘
```

## Service Dependencies

```
PostServiceImpl
    │
    ├─► PostRepository
    ├─► UserRepository
    ├─► GroupRepository
    ├─► PollOptionRepository
    ├─► ReactionRepository
    ├─► BookmarkRepository
    └─► FileUploadService
            │
            └─► Cloudinary

ReactionServiceImpl
    │
    ├─► ReactionRepository
    ├─► PostRepository
    ├─► CommentRepository
    └─► UserRepository

BookmarkServiceImpl
    │
    ├─► BookmarkRepository
    ├─► PostRepository
    ├─► UserRepository
    └─► PostServiceImpl (for mapping)
```

## Security Flow

```
HTTP Request
    │
    ▼
┌───────────────────┐
│ Security Filter   │  - JWT validation
│ Chain             │  - Extract user details
└─────────┬─────────┘
          │
          ▼ (Authenticated)
┌───────────────────┐
│ Controller        │  - Extract user ID
│                   │  - Pass to service
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ Service           │  - Check ownership
│                   │  - Verify permissions
│                   │  - Execute operation
└───────────────────┘
```

## Transaction Management

```
Controller (No Transaction)
    │
    ▼
Service (@Transactional)
    │
    ├─► Begin Transaction
    │
    ├─► Repository Call 1
    │
    ├─► Repository Call 2
    │
    ├─► External Service (FileUpload)
    │
    ├─► Repository Call 3
    │
    └─► Commit Transaction
        │
        └─► (On Exception: Rollback)
```

## Caching Strategy (Future)

```
┌─────────────────────────────────────────────────────────────────┐
│                      Caching Layers                              │
└─────────────────────────────────────────────────────────────────┘

Request
    │
    ▼
┌───────────────────┐
│ Application Cache │  - Frequently accessed posts
│ (Redis/Caffeine)  │  - User profiles
│                   │  - Reaction counts
└─────────┬─────────┘
          │ Cache Miss
          ▼
┌───────────────────┐
│ Database          │  - Query execution
│                   │  - Result set
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ CDN (Cloudinary)  │  - Media files
│                   │  - Transformed images
└───────────────────┘
```

## Scalability Considerations

```
┌─────────────────────────────────────────────────────────────────┐
│                    Horizontal Scaling                            │
└─────────────────────────────────────────────────────────────────┘

Load Balancer
    │
    ├─► App Instance 1 (PostService)
    │
    ├─► App Instance 2 (PostService)
    │
    └─► App Instance 3 (PostService)
            │
            ▼
    ┌──────────────────┐
    │ Database Cluster │
    │                  │
    │ Master (Write)   │
    │ Replica (Read)   │
    │ Replica (Read)   │
    └──────────────────┘
```

## Error Handling Flow

```
Exception Occurs
    │
    ▼
Service Layer
    │
    ├─► ApiException (400/404)
    │   └─► Custom error message
    │
    ├─► ValidationException (400)
    │   └─► Field validation errors
    │
    └─► RuntimeException (500)
        └─► Generic error message
    │
    ▼
@ControllerAdvice
    │
    ├─► Log error
    ├─► Format error response
    └─► Return HTTP status + JSON
```

## Monitoring Points

```
┌─────────────────────────────────────────────────────────────────┐
│                      Monitoring Layers                           │
└─────────────────────────────────────────────────────────────────┘

1. Application Metrics
   - Request count per endpoint
   - Response time
   - Error rate
   - Active users

2. Database Metrics
   - Query execution time
   - Connection pool usage
   - Slow queries
   - Deadlocks

3. External Service Metrics
   - Cloudinary upload success rate
   - Upload time
   - Storage usage
   - API quota

4. Business Metrics
   - Posts created per day
   - Reaction distribution
   - Most active users
   - Popular content
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Production Deployment                         │
└─────────────────────────────────────────────────────────────────┘

Internet
    │
    ▼
┌───────────────────┐
│ Load Balancer     │  - SSL Termination
│ (Nginx/AWS ALB)   │  - Rate limiting
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ Application       │  - Spring Boot
│ Servers (Docker)  │  - Post Service
│                   │  - Auto-scaling
└─────────┬─────────┘
          │
          ├─► PostgreSQL (RDS/Managed)
          │
          ├─► Redis (ElastiCache)
          │
          └─► Cloudinary (SaaS)
```

---

## Key Architectural Decisions

### 1. **Layered Architecture**
- Clear separation of concerns
- Easy to test and maintain
- Scalable and modular

### 2. **Repository Pattern**
- Abstraction over data access
- Easy to switch databases
- Testable with mocks

### 3. **DTO Pattern**
- Decoupling of API and domain models
- Version control for API
- Security (don't expose entities)

### 4. **JSONB for Flexible Data**
- Tags, mentions, media items stored as JSONB
- Flexible schema
- Efficient querying

### 5. **External Media Storage**
- Cloudinary for scalability
- CDN for fast delivery
- No local storage issues

### 6. **Composite Keys for Bookmarks**
- Natural key (user_id, post_id)
- Prevents duplicates
- Efficient queries

### 7. **Soft Reactions**
- Polymorphic reactions (posts/comments)
- Flexible reaction types
- Easy to add new types

---

This architecture provides a solid foundation for a scalable, maintainable social media post service.
