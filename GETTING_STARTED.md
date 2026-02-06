# Getting Started - Making SocialGod Runnable

## Current Status

Your application has a **solid foundation** but is missing critical business logic components:

### ✅ What You Have:
- Complete database schema (PostgreSQL)
- All JPA entities properly configured
- All repositories defined
- Configuration files (Security, WebSocket, Cache, etc.)
- DTO structures
- Main application entry point

### ❌ What's Missing (Where You Need to Code):
- **Controllers** - REST API endpoints (only 3 exist, need ~10 more)
- **Service Layer** - Business logic implementations (only 4 exist, need ~15 more)
- **Authentication** - Login/logout/registration logic
- **Database setup** - PostgreSQL installation and configuration

---

## Step 1: Setup PostgreSQL Database

### Option A: Local PostgreSQL Installation

1. **Install PostgreSQL 16**
   - Windows: Download from https://www.postgresql.org/download/windows/
   - Or use Chocolatey: `choco install postgresql`

2. **Start PostgreSQL and Create Database**
   ```sql
   -- Connect to PostgreSQL (default user: postgres)
   psql -U postgres
   
   -- Create database
   CREATE DATABASE socialhub;
   
   -- Verify
   \l
   ```

3. **Update Configuration**
   - The app already looks for these environment variables:
     - `DB_URL` (default: jdbc:postgresql://localhost:5432/socialhub)
     - `DB_USERNAME` (default: postgres)
     - `DB_PASSWORD` (default: postgres)
   
   - Either set environment variables OR edit `application.properties`:
   ```properties
   spring.datasource.password=your_postgres_password
   ```

### Option B: Docker PostgreSQL (Easier)

```bash
docker run --name socialgod-postgres \
  -e POSTGRES_DB=socialhub \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

---

## Step 2: Configure Email (Optional for now)

For password reset and OTP features, configure Gmail SMTP:

1. Get Gmail App Password: https://support.google.com/accounts/answer/185833
2. Set in `application.properties` or environment variables:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

**You can skip this initially** - the app will run without email configured.

---

## Step 3: Install Dependencies

```bash
# From project root (c:\SpringBoot\Hub)
mvn clean install
```

This will download all dependencies from `pom.xml`.

---

## Step 4: Run the Application

### Option A: Using Maven
```bash
mvn spring-boot:run
```

### Option B: Using IDE
Run `SocialApplication.java` main method

### Option C: Build and Run JAR
```bash
mvn clean package
java -jar target/social-0.0.1-SNAPSHOT.war
```

The application will start on: **http://localhost:8080**

---

## Step 5: Verify It's Running

Check these endpoints:

1. **Health Check**: http://localhost:8080/actuator/health
2. **Metrics**: http://localhost:8080/actuator/metrics
3. **Error Page**: http://localhost:8080/error

You should see JSON responses, not errors.

---

## Step 6: WHERE TO START CODING

Based on the architecture, here's your coding roadmap:

### Priority 1: Authentication (CRITICAL - Nothing works without this)

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\rest\AuthController.java`

This needs:
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login (already defined in SecurityConfig)
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\service\AuthService.java`

Implementation logic for registration, login validation, session management.

---

### Priority 2: User Management

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\rest\UserController.java`

Endpoints needed:
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update profile
- `POST /api/users/{id}/follow` - Follow user
- `DELETE /api/users/{id}/follow` - Unfollow user

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\service\UserService.java`

---

### Priority 3: Posts (Core Feature)

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\rest\PostController.java`

Endpoints needed:
- `POST /api/posts` - Create post
- `GET /api/posts` - Get feed (all posts)
- `GET /api/posts/{id}` - Get single post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `POST /api/posts/{id}/like` - Like post

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\service\PostService.java`

---

### Priority 4: Comments

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\rest\CommentController.java`

Endpoints needed:
- `POST /api/posts/{postId}/comments` - Add comment
- `GET /api/posts/{postId}/comments` - Get comments
- `PUT /api/comments/{id}` - Update comment
- `DELETE /api/comments/{id}` - Delete comment

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\service\CommentService.java`

---

### Priority 5: Groups

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\rest\GroupController.java`

Endpoints needed:
- `POST /api/groups` - Create group
- `GET /api/groups` - List groups
- `GET /api/groups/{id}` - Get group details
- `POST /api/groups/{id}/join` - Join group
- `POST /api/groups/{id}/leave` - Leave group

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\service\GroupService.java`

---

### Priority 6: Messaging (Advanced)

**File**: Create `c:\SpringBoot\Hub\src\main\java\com\example\demo\controller\websocket\ChatWebSocketHandler.java`

Real-time chat functionality using WebSocket.

---

## Quick Start Template

I'll create starter templates for the most critical components. Here's the coding order:

```
1. AuthController + AuthService (30 minutes)
2. UserController + UserService (45 minutes)  
3. PostController + PostService (1 hour)
4. CommentController + CommentService (30 minutes)
5. GroupController + GroupService (45 minutes)
```

**Total estimated time**: 3-4 hours for basic functionality

---

## Architecture Pattern to Follow

Always use this flow:

```
Controller (REST API)
    ↓
Service (Business Logic)
    ↓
Repository (Database Access)
    ↓
Entity (Database Table)
```

**Example**:
```
PostController.createPost()
    → PostService.createPost()
        → PostRepository.save()
            → Post entity
```

---

## Testing Strategy

### Manual Testing:
1. Use **Postman** or **Thunder Client** (VS Code extension)
2. Test each endpoint as you build it
3. Start with: Register → Login → Create Post → Get Posts

### Automated Testing:
- Unit tests for Services
- Integration tests for Controllers
- Currently: `PostServiceImplTest.java` exists as example

---

## Common Issues & Solutions

### Issue 1: Application won't start
**Solution**: Check PostgreSQL is running:
```bash
# Windows
sc query postgresql-x64-16

# Start if stopped
net start postgresql-x64-16
```

### Issue 2: Authentication errors
**Solution**: Remember to configure BCryptPasswordEncoder (already done in `SocialApplication.java`)

### Issue 3: CORS errors from frontend
**Solution**: Update `application.properties`:
```properties
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

### Issue 4: Flyway migration errors
**Solution**: Flyway is currently disabled. Schema is created via `schema.sql`. If needed:
```properties
spring.flyway.enabled=true
```

---

## Next Steps After Basic Setup

1. Add validation (@Valid annotations)
2. Add pagination for list endpoints
3. Add search/filter functionality
4. Implement file upload (Cloudinary already configured)
5. Add WebSocket for real-time features
6. Build React frontend

---

## Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **Project Structure**: See README.md
- **Performance Features**: See PERFORMANCE_UPGRADE.md
- **WebSocket Guide**: See WEBSOCKET_QUICK_REFERENCE.md

---

## Summary: What You Need to Do

1. ✅ **Setup PostgreSQL** (15 minutes)
2. ✅ **Run `mvn clean install`** (5 minutes)
3. ✅ **Start the application** (2 minutes)
4. 🔨 **Start coding controllers and services** (this is where you work!)

The infrastructure is ready. Now you build the business logic! Start with **AuthController** - nothing works without authentication.
