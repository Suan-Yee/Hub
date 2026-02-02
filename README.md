# Hub (Byte Bunnies)

Community social media application built with Spring Boot. Users can create posts, comment, like, join groups, chat, create polls, manage skills and topics, and receive notifications.

---

## Tech stack

| Layer | Technology |
|-------|------------|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 3.2.4 |
| **Packaging** | WAR (deployable to Tomcat) |
| **Database** | MySQL 8, JPA/Hibernate |
| **Migrations** | Flyway |
| **Security** | Spring Security (session-based) |
| **APIs** | REST, WebSocket, SSE |
| **Mail** | Spring Mail (Gmail SMTP) |
| **File storage** | Cloudinary |
| **Excel** | Apache POI (ooxml) |

---

## Prerequisites

- **JDK 21**
- **Maven 3.6+**
- **MySQL 8** (running, with a database for the app)
- (Optional) **Node.js** for a React frontend; CORS is configured for `http://localhost:3000`

---

## Quick start

### 1. Database

Create a MySQL database (e.g. `communityhub`):

```sql
CREATE DATABASE communityhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Apply schema and migrations with Flyway (runs on startup if configured).

### 2. Configuration

Copy or edit `src/main/resources/application.properties` and set at least:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/communityhub
spring.datasource.username=<your-db-user>
spring.datasource.password=<your-db-password>
```

For mail (password reset, OTP, etc.):

```properties
spring.mail.username=<your-email@gmail.com>
spring.mail.password=<app-password>
```

For Cloudinary (image uploads), configure `CloudinaryConfig` (e.g. env or properties).

### 3. Run

```bash
mvn clean spring-boot:run
```

Or build and run the WAR:

```bash
mvn clean package
java -jar target/Hub-0.0.1-SNAPSHOT.war
```

Default context is `/`. Use the configured port (e.g. `8080`) for REST and Web UI.

---

## Project structure (layered)

```
src/main/java/com/example/demo/
├── HubApplication.java              # Entry point, JPA auditing, async, scheduling
├── application/
│   ├── event/                       # Application events (e.g. notification ready)
│   └── usecase/                     # Service interfaces
│       └── impl/                    # Service implementations
├── config/                          # Security, WebMvc, WebSocket, Cloudinary, etc.
├── dto/                             # Request/response DTOs and records
│   ├── request/                     # Login, OTP, reset password, etc.
│   └── response/                    # Auth, profile, chat room, group request, etc.
├── dtoMapper/                       # Entity ↔ DTO mappers
├── entity/                          # JPA entities
├── enumeration/                     # Access, LikeType, MediaType, NotificationType, Role
├── exception/                       # Global exception handling, auth handlers
├── form/                            # Form objects (group, poll, comment, etc.)
├── infrastructure/
│   └── persistence/repository/      # JPA repositories
├── presentation/
│   ├── rest/                        # REST controllers
│   └── websocket/                   # WebSocket handlers (chat, comment)
└── utils/                           # Email, OTP, time formatting
```

**Architecture:** Controller → Service (usecase) → Repository → Entity. DTOs and mappers used at API boundaries.

---

## Main features

- **Auth:** Login (session), OTP send/verify, password reset, default-password check
- **Users:** Profile, list, search, role/policy management, image upload (Cloudinary)
- **Posts:** Create, update, delete, by user/topic/group, trending, counts
- **Comments & replies:** Per post, with WebSocket support
- **Likes:** Post likes and comment likes
- **Groups (communities):** Create, join, invite, kick, change admin/owner, group posts
- **Chat:** 1:1 and group chat; WebSocket; room list, messages, online users
- **Notifications:** List, mark read, toggle, count; SSE for real-time stream
- **Polls:** Create, vote, unvote, delete; per-group polls
- **Topics:** List, create; used for post categorization
- **Skills:** User skills (many-to-many with User); add, list by user, clear
- **Bookmarks:** Save posts; by topic; count
- **Guidelines:** Stored and served for community rules

---

## Main entities

| Entity | Purpose |
|--------|--------|
| `User` | Account, profile, roles, skills, groups, posts, likes, comments, chat rooms |
| `Post` | Content (text + media), user, group, topics |
| `Content` | Text body; one-to-one with Post; has `Media` list |
| `Media` | Images/videos/files linked to content (type, URL) |
| `Comment` | On posts; optional parent for replies |
| `Like` | Post or comment like (type: post/comment) |
| `CommentLike` | Like on a comment |
| `Group` | Community; has owner, members, posts |
| `UserHasGroup` | User–group membership |
| `GroupMessage` | Message in a group chat |
| `ChatRoom` | 1:1 or group chat room |
| `UserRoom` | User–chat room association |
| `ChatMessage` | Message in a chat room |
| `ChatRoomRequest` | Request to start a chat |
| `Notification` | In-app notification (post/comment, read status, type) |
| `Poll` / `PollOption` | Poll and options; voting tracked |
| `Topic` | Category for posts |
| `PostTopic` | Post–topic many-to-many |
| `Skill` | Skill name; many-to-many with User |
| `BookMark` | User bookmarks a post |
| `Mention` | User mention in content |
| `UserInvitation` | Group invitation |
| `UserRequestGroup` | Request to join a group |
| `OTP` | One-time password for auth |
| `GuideLines` | Community guidelines |

---

## API overview (REST)

Base URL is the app context (e.g. `http://localhost:8080`). Most endpoints require authentication (session cookie); exceptions are listed in **Public endpoints**.

### Public endpoints (no login)

- `POST /sendCode` – Send OTP (e.g. email)
- `POST /verify/**` – Verify OTP
- `POST /resetpassword` – Reset password
- `POST /api/auth/login` – Login
- `GET/POST /error/**` – Error handling

### Auth

- `POST /api/auth/login` – Login
- `POST /api/auth/logout` – Logout

### User & profile (`/user`, `UserApi`, `UserController`)

- Profile, list, search, upload image, change password, default-password check, role/policy, notifications, etc.

### Posts (`/post`, `PostRestController`)

- CRUD, by user/topic/group, trending, counts, group posts, delete

### Comments (`/post/comments`, `/post/reply`, `CommentController`)

- Get comments and replies for a post

### Likes (`/likes`, `LikeController`)

- Like post; get likes by user

### Comment likes (`/comment/like`, `CommentLikeController`)

- Like a comment

### Groups (`/group`, `GroupController`)

- Create community/group, list, details, members, posts, request to join, kick, change admin/owner, rebuild, etc.

### Chat (`ChatController`)

- Online users, messages (1:1 and group), room list, room members, upload/send photo

### Notifications (`/noti`, `NotificationController`)

- List, mark read, count, toggle

### SSE (`/sse`, `NotificationSseController`)

- `GET /sse/notifications` – Server-Sent Events stream for real-time notifications

### Polls (`PollRestController`)

- Create poll, list, vote/unvote, delete, by group

### Topics (`/topic`, `TopicController`)

- List all, create

### Skills (`SkillController`)

- Save skills, get by user, edit (replace) skills

### Bookmarks (`/bookmark`, `BookMarkController`)

- Save, list all, by topic, total count

### Invitations (`/user`, `UserInviteController`)

- Send invitation, list invited, accept/decline, count

### Group requests (`UserRequestGroupController`)

- Create request, accept

### OTP (`OTPRestController`)

- `POST /verify-OTPCode` – Verify OTP code

### Error

- `CustomErrorController` – Handles `/error` (e.g. 404).

---

## WebSocket

- **Chat:** `ChatWebSocketHandler` – real-time chat messages.
- **Comments:** `CommentWebSocketHandler` – real-time comment updates.

WebSocket paths and allowed origins are set in `WebSocketConfig`.

---

## Security

- **Session-based:** Login returns session cookie; subsequent requests use it.
- **CSRF:** Disabled for REST/API usage (re-enable if needed for form-based UIs).
- **Public paths:** Only the login, OTP, reset-password, and error endpoints are public; all other requests require authentication.
- **401:** Unauthenticated requests get JSON `{"status":401,"error":"Unauthorized","message":"Authentication required"}`.

---

## Configuration summary

| Property / area | Purpose |
|-----------------|--------|
| `spring.datasource.*` | MySQL connection |
| `spring.jpa.hibernate.ddl-auto` | Set to `validate` (schema via Flyway) |
| `spring.flyway.*` | Migrations (e.g. `baseline-on-migrate`) |
| `spring.mail.*` | SMTP for OTP and password reset |
| `spring.servlet.multipart.*` | Max file size for uploads |
| `app.cors.allowed-origins` | CORS (e.g. React at `http://localhost:3000`) |
| `server.error.*` | Error page and path |
| Cloudinary | Configured in `CloudinaryConfig` for image uploads |

---

## Build & test

```bash
mvn clean package
mvn test
```

---

## Version

- **Artifact:** `Hub` `0.0.1-SNAPSHOT`
- **App title:** Byte Bunnies (from `spring.application.title`)
