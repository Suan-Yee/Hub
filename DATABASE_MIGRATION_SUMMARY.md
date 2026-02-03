# Database Migration Summary

## Overview
Successfully migrated from MySQL to PostgreSQL with a completely redesigned schema optimized for a modern social media platform.

## Date
February 3, 2026

---

## Schema Changes

### New Database: PostgreSQL
- **Database Name**: `socialhub`
- **Previous**: MySQL (`socialgod`)
- **Reason**: Better support for JSONB, arrays, and advanced features

### New Tables Created

#### 1. **users**
Replaces: `User`, `AppUser`
- Modern user management with privacy settings
- Fields: username, email, password_hash, bio, avatar_url, is_verified, is_private_account, allow_messages_from
- Timestamps: created_at, last_active_at

#### 2. **user_relations**
Replaces: Previous follow/friendship logic
- Follower/Following relationships
- Support for pending follow requests (private accounts)
- Fields: follower_id, following_id, status ('pending', 'accepted')

#### 3. **user_blocks**
New feature
- User blocking functionality for trust & safety
- Fields: blocker_id, blocked_id, created_at

#### 4. **groups**
Replaces: `Group`
- Enhanced with privacy types
- Fields: name, description, privacy_type ('public', 'private'), owner_id, cover_image_url

#### 5. **group_members**
Replaces: `UserHasGroup`
- Group membership with roles
- Fields: group_id, user_id, role ('admin', 'moderator', 'member')

#### 6. **posts**
Replaces: `Post`, `Content`
- Unified content model with type discrimination
- JSONB support for media_urls and link_metadata
- Array support for hashtags
- Fields: type ('text', 'image', 'video', 'poll', 'repost'), caption, visibility
- Metrics: likes_count, comments_count, shares_count
- Repost support: original_post_id

#### 7. **poll_options**
Replaces: `PollOption`
- Poll options for poll posts
- Fields: post_id, option_text, vote_count

#### 8. **poll_votes**
Replaces: Previous poll voting logic
- Tracks individual poll votes
- Composite key: (poll_option_id, user_id)

#### 9. **stories**
New feature
- Ephemeral content (24-hour stories)
- Fields: user_id, media_url, type, expires_at

#### 10. **comments**
Replaces: `Comment`
- Nested comment support with depth tracking
- Fields: post_id, user_id, content, parent_comment_id, depth

#### 11. **conversations**
Replaces: `ChatRoom`
- Unified messaging system
- Fields: type ('private', 'group'), name, last_message_at

#### 12. **conversation_participants**
Replaces: `UserRoom`
- Tracks conversation participants
- Composite key: (conversation_id, user_id)

#### 13. **messages**
Replaces: `ChatMessage`, `GroupMessage`
- Unified message model
- Fields: conversation_id, sender_id, content, media_url, is_read

#### 14. **reactions**
Replaces: `Like`, `CommentLike`
- Polymorphic reactions for posts and comments
- Multiple reaction types: 'like', 'love', 'haha', 'sad', 'angry'
- Fields: user_id, target_type ('post', 'comment'), target_id, reaction_type

#### 15. **bookmarks**
Replaces: `BookMark`
- User bookmarks for posts
- Composite key: (user_id, post_id)

#### 16. **hashtags**
New feature
- Hashtag management with usage tracking
- Fields: tag, usage_count

#### 17. **post_hashtags**
New feature
- Many-to-many relationship between posts and hashtags
- Composite key: (post_id, hashtag_id)

#### 18. **content_reports**
New feature - Trust & Safety
- Content moderation and reporting system
- Fields: reporter_id, target_type ('post', 'comment', 'user'), target_id, reason, status, admin_notes

---

## Deleted Entities

### Entities Removed:
1. `AppUser.java`
2. `BookMark.java` → Replaced with `Bookmark.java`
3. `ChatMessage.java` → Merged into `Message.java`
4. `ChatRoom.java` → Replaced with `Conversation.java`
5. `ChatRoomRequest.java` → No longer needed
6. `CommentLike.java` → Merged into `Reaction.java`
7. `Content.java` → Merged into `Post.java`
8. `GroupMessage.java` → Merged into `Message.java`
9. `GuideLines.java` → Removed (can be re-implemented as needed)
10. `Like.java` → Merged into `Reaction.java`
11. `Media.java` → Replaced with JSONB in `Post.java`
12. `Mention.java` → Can be re-implemented as needed
13. `Notification.java` → Can be re-implemented as needed
14. `OTP.java` → Can be re-implemented as needed
15. `Poll.java` → Merged into `Post.java` with type='poll'
16. `PostTopic.java` → Replaced with hashtags
17. `Skill.java` → Can be added to User as JSONB if needed
18. `Topic.java` → Replaced with hashtags
19. `UserHasGroup.java` → Replaced with `GroupMember.java`
20. `UserInvitation.java` → Can be re-implemented as needed
21. `UserRequestGroup.java` → Can be re-implemented as needed
22. `UserRoom.java` → Replaced with `ConversationParticipant.java`

### Repositories Removed:
All repositories for the above entities (25 repositories deleted)

### Services Removed:
All service interfaces and implementations for the above entities (24 services + 24 implementations deleted)

---

## New Entities Created

### Core Entities (18 total):
1. `User.java` - Main user entity
2. `UserRelation.java` - Follow relationships
3. `UserBlock.java` - Blocking functionality
4. `Group.java` - Groups/communities
5. `GroupMember.java` - Group membership
6. `Post.java` - Universal post entity
7. `PollOption.java` - Poll options
8. `PollVote.java` - Poll voting
9. `Story.java` - Ephemeral stories
10. `Comment.java` - Post comments
11. `Conversation.java` - Chat conversations
12. `ConversationParticipant.java` - Chat participants
13. `Message.java` - Chat messages
14. `Reaction.java` - Polymorphic reactions
15. `Bookmark.java` - Post bookmarks
16. `Hashtag.java` - Hashtag management
17. `PostHashtag.java` - Post-hashtag mapping
18. `ContentReport.java` - Content moderation

---

## New Repositories Created

All 18 new repositories with optimized queries for PostgreSQL:
1. `UserRepository`
2. `UserRelationRepository`
3. `UserBlockRepository`
4. `GroupRepository`
5. `GroupMemberRepository`
6. `PostRepository`
7. `PollOptionRepository`
8. `PollVoteRepository`
9. `StoryRepository`
10. `CommentRepository`
11. `ConversationRepository`
12. `ConversationParticipantRepository`
13. `MessageRepository`
14. `ReactionRepository`
15. `BookmarkRepository`
16. `HashtagRepository`
17. `PostHashtagRepository`
18. `ContentReportRepository`

---

## Configuration Changes

### application.properties Updates:
- Changed database URL to PostgreSQL: `jdbc:postgresql://localhost:5432/socialhub`
- Changed dialect to `PostgreSQLDialect`
- Added PostgreSQL-specific properties
- Changed ddl-auto from `validate` to `update` for development
- Added schema.sql initialization
- Disabled Flyway temporarily (can be re-enabled later)

### New Files:
- `schema.sql` - Complete PostgreSQL schema with all tables and indexes

---

## Key Improvements

### 1. **Simplified Architecture**
- Reduced entity count from 27 to 18
- Unified similar entities (ChatMessage + GroupMessage → Message)
- Polymorphic design (Reaction replaces Like + CommentLike)

### 2. **Modern Features**
- JSONB support for flexible data (media_urls, link_metadata)
- Array support for hashtags in posts
- Ephemeral content (Stories)
- Advanced reactions (beyond just "like")
- Content moderation system
- User blocking

### 3. **Better Performance**
- Comprehensive indexes on all foreign keys
- Optimized queries in repositories
- Denormalized counters (likes_count, comments_count, etc.)
- GIN index on hashtags array

### 4. **Scalability**
- PostgreSQL's advanced features
- Better support for JSON data
- Improved query optimization
- Timestamp with timezone support

---

## Migration Steps

### To Complete Migration:

1. **Install PostgreSQL**
   ```bash
   # Install PostgreSQL 15 or higher
   # Create database: socialhub
   ```

2. **Update Dependencies**
   Add to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

3. **Configure Database**
   Set environment variables or update application.properties:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/socialhub
   DB_USERNAME=postgres
   DB_PASSWORD=your_password
   ```

4. **Run Application**
   The schema will be automatically created on first run due to `spring.jpa.hibernate.ddl-auto=update`

5. **Data Migration** (if needed)
   - Export data from old MySQL database
   - Transform data to match new schema
   - Import into PostgreSQL

---

## Next Steps

### Services to Recreate (as needed):
1. Authentication & User Services
2. Post Management Services
3. Comment Services
4. Group Management Services
5. Messaging Services
6. Notification Services (new implementation)
7. Story Services (new feature)
8. Hashtag Services (new feature)
9. Content Moderation Services (new feature)

### Additional Features to Consider:
1. Full-text search on posts and comments
2. Real-time notifications (SSE or WebSocket)
3. Image upload and processing
4. Story expiration scheduler
5. Rate limiting
6. Admin dashboard for content moderation

---

## Notes

- All entities use Java 21 features and Lombok
- Constructor injection pattern (no @Autowired)
- Proper use of `@Builder` with default collections
- Comprehensive indexes for performance
- Foreign key constraints with proper CASCADE rules
- Timestamp with timezone for proper time handling
- Composite keys for join tables

---

## Breaking Changes

⚠️ **Important**: This is a breaking change that requires:
1. Complete database migration
2. Service layer rewrite
3. Controller updates to use new entities
4. DTO updates
5. Frontend updates for new API contracts

The old MySQL database and entities are no longer compatible with this version.
