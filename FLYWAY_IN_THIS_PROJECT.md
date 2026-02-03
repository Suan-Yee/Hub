# How Flyway Helps in This Project (SocialGod)

Flyway is already in your project and runs on every application startup. Here’s what it does and how to use it well.

---

## What Flyway Does Here

### 1. **Versioned schema changes**

- **Location:** `src/main/resources/db/migration/`
- **Naming:** `V<version>__<description>.sql` (e.g. `V1__create_media_and_migrate.sql`, `V2__drop_user_mention.sql`)
- On startup, Flyway:
  - Connects to the same DB as your app (from `spring.datasource.*`)
  - Creates a table `flyway_schema_history` if it doesn’t exist
  - Runs any migration that hasn’t been run yet, in order
  - Records each run in `flyway_schema_history`

So schema and data changes are **versioned** and **repeatable**: same SQL runs the same way on every environment (dev, staging, prod) in order.

### 2. **Works with `ddl-auto=validate`**

You use:

- **Prod/default:** `spring.jpa.hibernate.ddl-auto=validate` → Hibernate does **not** create or alter tables; it only checks that the DB matches the entities.
- **Dev (optional):** `spring.jpa.hibernate.ddl-auto=update` in `application-dev.properties` for local iteration.

So in production, **Flyway owns** schema changes; Hibernate only validates. That avoids:

- Accidental schema drift from Hibernate
- Different DB state on each server
- “Works on my machine” because one env had `update` and another didn’t

Flyway ensures the **same** migrations run **everywhere** in a controlled way.

### 3. **What you already have**

| Migration | Purpose |
|-----------|---------|
| `V1__create_media_and_migrate.sql` | Creates `media` table, migrates data from `image`/`video`/`file`, drops old tables |
| `V2__drop_user_mention.sql` | Drops `user_mention` table |

So Flyway is already helping by:

- Applying these changes in a fixed order on every new or updated DB
- Keeping a history of what ran and when

---

## How Flyway Helps Going Forward

### 1. **Safe production deployments**

- Put every schema or reference-data change in a new migration (e.g. `V3__add_notification_read_at.sql`).
- Deploy: start the app → Flyway runs pending migrations → app runs with new schema.
- No manual “run this SQL on prod”; the app carries the migrations.

### 2. **Consistent environments**

- Dev, CI, staging, prod all run the same set of migrations from the same codebase.
- New dev: create DB → start app → Flyway runs all migrations → DB matches everyone else.
- Reduces “schema out of sync” and “forgot to run that script” issues.

### 3. **Rollback and history**

- Flyway doesn’t auto-rollback, but `flyway_schema_history` is a clear record of what ran.
- For rollback you either:
  - Add a new migration that reverses a change (recommended), or
  - Use Flyway’s undo/repair features if you use that edition.
- You always know which version the DB is at (from `flyway_schema_history`).

### 4. **Team and CI**

- Migrations are in Git; code and DB changes are in one place.
- Code review includes SQL; no “I ran something on prod and didn’t commit it.”
- CI can start a fresh DB, run Flyway, then run tests against the real schema.

---

## Configuration in This Project

**Current (application.properties):**

```properties
spring.flyway.baseline-on-migrate=true
```

- **baseline-on-migrate=true:** If the DB already exists and has no `flyway_schema_history`, Flyway treats it as “baseline” (version 0) and runs all migrations from V1 onward. Good for adopting Flyway on an existing DB (e.g. socialgod that was created without Flyway).

**Optional settings you can add:**

```properties
# Default: classpath:db/migration
# spring.flyway.locations=classpath:db/migration

# Run migrations before the app uses the DB (recommended)
spring.flyway.migrate-on-startup=true

# Validate that applied migrations haven’t been changed (checksum)
spring.flyway.validate-on-migrate=true

# If you use multiple schemas (e.g. MySQL databases)
# spring.flyway.schemas=socialgod
```

---

## Adding a New Migration

1. **Create a new file** in `src/main/resources/db/migration/`:
   - Name: `V<next_number>__<short_description>.sql`
   - Example: `V3__add_notification_read_at.sql`
   - Use **two underscores** between version and description.

2. **Write plain SQL** (DDL and/or DML), e.g.:

```sql
-- V3__add_notification_read_at.sql
ALTER TABLE notification ADD COLUMN read_at TIMESTAMP NULL;
CREATE INDEX idx_notification_read_at ON notification(read_at);
```

3. **Deploy:** next time the app starts, Flyway will run `V3` (and any later ones) in order.

Rules:

- **Never edit** a migration that has already run in any environment (change checksum breaks validation). Fix forward with a new migration.
- **One logical change per migration** (e.g. one new column, one new table) keeps history clear and rollback easier.

---

## Summary: How Flyway Helps in This Project

| Area | How Flyway helps |
|------|-------------------|
| **Schema ownership** | Flyway applies changes; Hibernate validates. Production stays predictable. |
| **Environments** | Same migrations everywhere → same schema in dev, CI, staging, prod. |
| **History** | `flyway_schema_history` shows what ran and when. |
| **Team** | Migrations in Git; schema changes are reviewed and versioned with code. |
| **Existing DB** | `baseline-on-migrate=true` lets you adopt Flyway on socialgod without losing data. |

So in this project, Flyway helps by **owning** schema and one-off data changes in a versioned, repeatable way, while you safely use `ddl-auto=validate` in production.
