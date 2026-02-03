# Cleanup Summary - Old Entity Related Code

## Date: February 3, 2026

This document tracks all files deleted during the migration from old entity structure to the new PostgreSQL-based schema.

---

## Total Files Deleted: 75+

---

## 1. Entities Deleted (22 files)

### Core Entities
1. `AppUser.java` - Replaced with `User.java`
2. `BookMark.java` - Replaced with `Bookmark.java` (capitalization change)
3. `ChatMessage.java` - Merged into `Message.java`
4. `ChatRoom.java` - Replaced with `Conversation.java`
5. `ChatRoomRequest.java` - Removed (no longer needed)
6. `CommentLike.java` - Merged into `Reaction.java`
7. `Content.java` - Merged into `Post.java`
8. `GroupMessage.java` - Merged into `Message.java`
9. `GuideLines.java` - Removed (can be re-implemented)
10. `Like.java` - Merged into `Reaction.java`
11. `Media.java` - Replaced with JSONB in `Post.java`
12. `Mention.java` - Removed (can be re-implemented)
13. `Notification.java` - Removed (needs re-implementation)
14. `OTP.java` - Removed (needs re-implementation)
15. `Poll.java` - Merged into `Post.java` with type='poll'
16. `PostTopic.java` - Replaced with hashtags
17. `Skill.java` - Removed (can be user JSONB)
18. `Topic.java` - Replaced with hashtags
19. `UserHasGroup.java` - Replaced with `GroupMember.java`
20. `UserInvitation.java` - Removed (needs re-implementation)
21. `UserRequestGroup.java` - Removed (needs re-implementation)
22. `UserRoom.java` - Replaced with `ConversationParticipant.java`

---

## 2. Repositories Deleted (25 files)

All repositories for the deleted entities:

1. `BookMarkRepository.java`
2. `ChatMessageRepository.java`
3. `ChatRoomRepository.java`
4. `CommentLikeRepository.java`
5. `CommentRepository.java`
6. `ContentRepository.java`
7. `GroupMessageRepository.java`
8. `GroupRepository.java`
9. `GuideLinesRepository.java`
10. `LikeRepository.java`
11. `MediaRepository.java`
12. `MentionRepository.java`
13. `NotificationRepository.java`
14. `OtpRepository.java`
15. `PollOptionRepository.java`
16. `PollRepository.java`
17. `PostRepository.java`
18. `PostTopicRepository.java`
19. `SkillRepository.java`
20. `TopicRepository.java`
21. `UserHasGroupRepository.java`
22. `UserInvitationRepository.java`
23. `UserRepository.java`
24. `UserRequestGroupRepository.java`
25. `UserRoomRepository.java`

---

## 3. Services Deleted (48 files)

### Service Interfaces (24 files)
1. `BookMarkService.java`
2. `ChatMessageService.java`
3. `ChatRoomService.java`
4. `CommentLikeService.java`
5. `CommentService.java`
6. `ContentService.java`
7. `GroupMessageService.java`
8. `GroupService.java`
9. `GuideLinesService.java`
10. `LikeService.java`
11. `MediaService.java`
12. `MentionService.java`
13. `NotificationService.java`
14. `PollOptionService.java`
15. `PollService.java`
16. `PostService.java`
17. `PostTopicService.java`
18. `SkillService.java`
19. `TopicService.java`
20. `UserHasGroupService.java`
21. `UserInvitationService.java`
22. `UserRequestGroupService.java`
23. `UserRoomService.java`
24. `UserService.java`

### Service Implementations (24 files)
1. `BookMarkServiceImpl.java`
2. `ChatMessageServiceImpl.java`
3. `ChatRoomServiceImpl.java`
4. `CommentLikeServiceImpl.java`
5. `CommentServiceImpl.java`
6. `ContentServiceImpl.java`
7. `GroupMessageServiceImpl.java`
8. `GroupServiceImpl.java`
9. `GuideLinesServiceImpl.java`
10. `LikeServiceImpl.java`
11. `MediaServiceImpl.java`
12. `MentionServiceImpl.java`
13. `NotificationServiceImpl.java`
14. `PollOptionServiceImpl.java`
15. `PollServiceImpl.java`
16. `PostServiceImpl.java`
17. `PostTopicServiceImpl.java`
18. `SkillServiceImpl.java`
19. `TopicServiceImpl.java`
20. `UserHasGroupServiceImpl.java`
21. `UserInvitationServiceImpl.java`
22. `UserRequestGroupServiceImpl.java`
23. `UserRoomServiceImpl.java`
24. `UserServiceImpl.java`

---

## 4. DTOs Deleted (18 files)

### Main DTOs
1. `BookMarkDto.java`
2. `ChatMessageDto.java`
3. `CommentDto.java`
4. `ContentDto.java`
5. `GroupDto.java`
6. `GroupMessageDto.java`
7. `InvitationDto.java`
8. `LikeDto.java`
9. `MediaDto.java`
10. `MentionDto.java`
11. `NotificationDto.java`
12. `NotificationChatRoomDto.java`
13. `PollDto.java`
14. `PollOptionReportDto.java`
15. `PostDto.java`
16. `TopicDto.java`
17. `UserDto.java`
18. `UserInvitationDto.java`
19. `UserRequestGroupDto.java`
20. `GroupPostPopulation.java`
21. `GroupReport.java`

### Response DTOs (4 files)
1. `ChatRoomResponse.java`
2. `GroupRequestResponse.java`
3. `UserListResponse.java`
4. `UserProfileResponse.java`

---

## 5. DTO Mappers Deleted (7 files)

1. `GroupPostPopulationMapper.java`
2. `GroupReportMapper.java`
3. `PollDtoMapper.java`
4. `PollOptionReportDtoMapper.java`
5. `PostDtoMapper.java`
6. `TopicDtoMapper.java`
7. `UserDtoMapper.java`

---

## 6. Controllers Deleted (17 files)

### REST Controllers (15 files)
1. `AuthController.java` - References old UserService
2. `BookMarkController.java`
3. `ChatController.java`
4. `CommentController.java`
5. `CommentLikeController.java`
6. `GroupController.java`
7. `LikeController.java`
8. `LoginController.java`
9. `NotificationController.java`
10. `OTPRestController.java` - References old OTP entity
11. `PollRestController.java`
12. `PostRestController.java`
13. `SkillController.java`
14. `TopicController.java`
15. `UserApi.java` - Large controller with many old service dependencies
16. `UserController.java`
17. `UserInviteController.java`
18. `UserRequestGroupController.java`

### WebSocket Handlers (2 files)
1. `ChatWebSocketHandler.java`
2. `CommentWebSocketHandler.java`

---

## 7. Event Handlers Deleted (2 files)

1. `NotificationReadyEvent.java`
2. `NotificationReadyEventListener.java`

---

## 8. Files Modified

### Updated Files
1. **`application.properties`**
   - Changed from MySQL to PostgreSQL
   - Updated database URL, driver, dialect
   - Changed ddl-auto from `validate` to `update`
   - Added PostgreSQL-specific settings
   - Disabled Flyway temporarily

2. **`pom.xml`**
   - Added PostgreSQL driver dependency
   - Changed Flyway from `flyway-mysql` to `flyway-database-postgresql`
   - Made MySQL connector optional (for migration purposes)

3. **`DataBaseInitialization.java`**
   - Updated to use new User entity structure
   - Changed from UserService to UserRepository
   - Updated field names to match new schema
   - Uses `OffsetDateTime` instead of `LocalDateTime`

---

## Files Kept (Still Working)

### Remaining Controllers (4 files)
1. `CustomErrorController.java` - Error handling
2. `NotificationSseController.java` - SSE notifications
3. `UserApi.java` - ❌ DELETED (had too many old dependencies)
4. `WebSocketMetricsController.java` - WebSocket metrics

### Remaining Services (6 files + implementations)
1. `CachedDataService.java` - Caching utility
2. `EmailService.java` + `EmailServiceImpl.java` - Email functionality
3. `ExcelUploadService.java` + `ExcelUploadServiceImpl.java` - Excel import
4. `FileUploadService.java` + `FileUploadServiceImpl.java` - File uploads
5. `OnlineStatusService.java` + `OnlineStatusServiceImpl.java` - User status
6. `OtpService.java` + `OtpServiceImpl.java` - OTP management (needs update)

### Configuration Files (All kept)
1. `AsyncConfig.java`
2. `CacheConfig.java`
3. `CloudinaryConfig.java`
4. `DatabaseConfig.java`
5. `DataBaseInitialization.java` - ✅ UPDATED
6. `NotificationSseEmitterStore.java`
7. `PerformanceConfig.java`
8. `QueryPerformanceConfig.java`
9. `SecurityConfig.java`
10. `WebMvcConfig.java`
11. `WebSocketConfig.java`
12. `WebSocketEventListener.java`
13. `WebSocketHeartbeatHandler.java`
14. `WebSocketProperties.java`
15. `WebSocketScheduledTasks.java`
16. `WebSocketSessionManager.java`

### Enumerations (All kept)
1. `Access.java`
2. `LikeType.java`
3. `MediaType.java`
4. `NotificationType.java`
5. `Role.java`

### Forms (All kept)
1. `ChangeDefaultPassword.java`
2. `ChangePasswordInput.java`
3. `GroupForm.java`
4. `GroupUserDisplay.java`
5. `PollCreationForm.java`
6. `UserCommentForm.java`
7. `UserIdAndOTPViewObject.java`
8. `UserKickForm.java`
9. `UserRequestGroupCheck.java`
10. `UserVotedPollOption.java`
11. `VoteRemoveForm.java`

### Request DTOs (All kept)
1. `LoginRequest.java`
2. `ResetPasswordRequest.java`
3. `SendCodeRequest.java`
4. `VerifyOtpRequest.java`

### Response DTOs (1 kept)
1. `AuthResponse.java`

### Utilities (All kept)
1. `EmailUtils.java`
2. `OTPGenerator.java`
3. `TimeFormatter.java`

### Exception Handlers (All kept)
1. `ApiException.java`
2. `CustomAccessDeniedHandler.java`
3. `CustomAuthenticationEntryPoint.java`
4. `CustomAuthenticationSuccessHandler.java`
5. `SocialGodException.java`
6. `SocialGodExceptionHandler.java`

---

## Summary Statistics

### Deleted:
- **Entities**: 22
- **Repositories**: 25
- **Services**: 48 (24 interfaces + 24 implementations)
- **DTOs**: 25
- **Mappers**: 7
- **Controllers**: 17
- **WebSocket Handlers**: 2
- **Event Handlers**: 2
- **Total Files Deleted**: ~148 files

### Modified:
- **Configuration Files**: 2 (application.properties, pom.xml)
- **Initialization Files**: 1 (DataBaseInitialization.java)

### Kept:
- **New Entities**: 18
- **New Repositories**: 18
- **Utility Services**: 6 interfaces + 6 implementations
- **Configuration**: 16 files
- **Exceptions**: 6 files
- **Forms**: 11 files
- **Enumerations**: 5 files
- **Utilities**: 3 files

---

## Next Steps

### Immediate (Required to Run)
1. ✅ Create new repositories - DONE
2. ✅ Update configuration files - DONE
3. ❌ Create new service interfaces for core entities
4. ❌ Implement new services
5. ❌ Create new controllers
6. ❌ Create new DTOs (using Java Records)

### Secondary (Nice to Have)
1. Re-implement notification system
2. Re-implement OTP functionality with new User entity
3. Create WebSocket handlers for new schema
4. Update forms to match new entities
5. Create data migration scripts (if needed)

### Testing
1. Test database schema creation
2. Test basic CRUD operations
3. Test authentication (needs re-implementation)
4. Test file upload functionality
5. Integration tests

---

## Notes

- All utility services (Email, File Upload, Excel) are preserved as they don't depend on old entities
- Security configuration is preserved but may need updates for new authentication flow
- WebSocket configuration is preserved but handlers need re-implementation
- Forms are kept but may need updates to work with new entities
- Enumerations are all preserved as they're generic

---

## Breaking Changes

⚠️ **This cleanup creates breaking changes:**

1. **No authentication** - AuthController and UserService deleted
2. **No API endpoints** - All main controllers deleted
3. **No business logic** - All service implementations deleted
4. **No DTOs** - All old DTOs deleted

The application will NOT function until new services and controllers are implemented!

---

## Files Safe to Use

These files are independent and will work without changes:
- All configuration files (except DataBaseInitialization - already updated)
- EmailService and implementation
- FileUploadService and implementation
- ExcelUploadService and implementation
- OnlineStatusService and implementation
- All exception handlers
- All utilities
- All enumerations
- NotificationSseController (but needs notification service)
- WebSocketMetricsController

---

## Migration Checklist

- [x] Delete old entities
- [x] Delete old repositories
- [x] Delete old services
- [x] Delete old DTOs
- [x] Delete old mappers
- [x] Delete old controllers
- [x] Create new entities
- [x] Create new repositories
- [x] Update configuration files
- [ ] Create new DTOs (Records)
- [ ] Create new services
- [ ] Create new controllers
- [ ] Update authentication
- [ ] Test database initialization
- [ ] Data migration (if needed)
