# Setup Guide - PostgreSQL Migration

## Prerequisites

1. **Java 21** installed
2. **Maven** installed
3. **PostgreSQL 15+** installed and running

---

## Step 1: Install and Configure PostgreSQL

### Windows:
1. Download PostgreSQL from https://www.postgresql.org/download/windows/
2. Install with default settings
3. Remember the password you set for the `postgres` user

### macOS:
```bash
brew install postgresql@15
brew services start postgresql@15
```

### Linux:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

---

## Step 2: Create Database

### Option A: Using psql command line
```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE socialhub;

# Exit
\q
```

### Option B: Using pgAdmin
1. Open pgAdmin
2. Right-click on Databases
3. Select "Create" → "Database"
4. Name: `socialhub`
5. Click "Save"

---

## Step 3: Configure Application

### Update Environment Variables (Recommended)
Create a `.env` file or set system environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/socialhub
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
```

### OR Update application.properties Directly
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/socialhub
spring.datasource.username=postgres
spring.datasource.password=your_password
```

---

## Step 4: Build the Project

```bash
# Clean and install dependencies
mvn clean install

# Or just compile
mvn clean compile
```

---

## Step 5: Run the Application

### Option A: Using Maven
```bash
mvn spring-boot:run
```

### Option B: Using IDE
1. Open project in IntelliJ IDEA or Eclipse
2. Run the main application class
3. Make sure PostgreSQL is running

### Option C: Using JAR
```bash
mvn clean package
java -jar target/social-0.0.1-SNAPSHOT.war
```

---

## Step 6: Verify Database Schema

The application will automatically create all tables on first run due to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Verify Tables Created:
```sql
-- Connect to database
psql -U postgres -d socialhub

-- List all tables
\dt

-- Expected tables:
-- users, user_relations, user_blocks, groups, group_members,
-- posts, poll_options, poll_votes, stories, comments,
-- conversations, conversation_participants, messages,
-- reactions, bookmarks, hashtags, post_hashtags, content_reports
```

---

## Step 7: Test the API

Once the application is running, test the endpoints:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Test API (example)
curl http://localhost:8080/api/v1/users
```

---

## Troubleshooting

### Issue: Connection Refused
**Solution**: Make sure PostgreSQL is running
```bash
# Check PostgreSQL status
sudo systemctl status postgresql  # Linux
brew services list  # macOS
```

### Issue: Authentication Failed
**Solution**: Check username and password in application.properties match PostgreSQL credentials

### Issue: Database Does Not Exist
**Solution**: Create the database manually:
```sql
CREATE DATABASE socialhub;
```

### Issue: Port Already in Use
**Solution**: Change the application port in application.properties:
```properties
server.port=8081
```

### Issue: Schema Creation Fails
**Solution**: 
1. Check PostgreSQL logs
2. Ensure user has proper permissions:
```sql
GRANT ALL PRIVILEGES ON DATABASE socialhub TO postgres;
```

---

## Development vs Production

### Development (Current Setup)
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Production (Recommended)
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.flyway.enabled=true
```

For production, use Flyway migrations instead of auto-update.

---

## Next Steps

1. **Create Services**: Implement business logic for new entities
2. **Create Controllers**: Build REST API endpoints
3. **Add Authentication**: Implement JWT-based auth
4. **Add Validation**: Add @Valid annotations to DTOs
5. **Write Tests**: Create unit and integration tests
6. **Set up CI/CD**: Configure deployment pipeline

---

## Useful PostgreSQL Commands

```sql
-- Connect to database
\c socialhub

-- List all tables
\dt

-- Describe table structure
\d users

-- View indexes
\di

-- View all constraints
\d+ users

-- Check table size
SELECT pg_size_pretty(pg_total_relation_size('users'));

-- View active connections
SELECT * FROM pg_stat_activity WHERE datname = 'socialhub';
```

---

## Migration from Old Database (Optional)

If you need to migrate data from the old MySQL database:

1. Export data from MySQL
2. Transform data to match new schema
3. Use a migration script or tool like pgLoader

Example export:
```sql
-- MySQL
SELECT * FROM user INTO OUTFILE '/tmp/users.csv';
```

Then import and transform into PostgreSQL's new schema.

---

## Support

For issues or questions:
1. Check the logs: `target/logs/spring.log`
2. Review `DATABASE_MIGRATION_SUMMARY.md` for schema details
3. Consult Spring Boot and PostgreSQL documentation
