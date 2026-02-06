-- ============================================================================
-- Post Service Database Migration Script
-- ============================================================================
-- This script updates the posts table to support the new Post Service features
-- Run this script on your PostgreSQL database before deploying the service
-- ============================================================================

-- Start transaction
BEGIN;

-- ============================================================================
-- STEP 1: Backup existing data (recommended)
-- ============================================================================
-- Create backup table
CREATE TABLE posts_backup AS SELECT * FROM posts;

-- Verify backup
SELECT COUNT(*) as backup_count FROM posts_backup;

-- ============================================================================
-- STEP 2: Add new columns
-- ============================================================================

-- Add mentions column (JSONB array of usernames)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS mentions JSONB DEFAULT '[]'::jsonb;

-- Add edited flag
ALTER TABLE posts ADD COLUMN IF NOT EXISTS edited BOOLEAN DEFAULT FALSE;

-- Add poll question column
ALTER TABLE posts ADD COLUMN IF NOT EXISTS poll_question TEXT;

-- ============================================================================
-- STEP 3: Rename columns
-- ============================================================================

-- Rename caption to content
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'posts' AND column_name = 'caption'
    ) THEN
        ALTER TABLE posts RENAME COLUMN caption TO content;
    END IF;
END $$;

-- Rename hashtags to tags
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'posts' AND column_name = 'hashtags'
    ) THEN
        ALTER TABLE posts RENAME COLUMN hashtags TO tags;
    END IF;
END $$;

-- ============================================================================
-- STEP 4: Convert media_urls to media_items structure
-- ============================================================================

-- Add new media_items column
ALTER TABLE posts ADD COLUMN IF NOT EXISTS media_items JSONB;

-- Migrate data from media_urls to media_items
-- This converts ["url1", "url2"] to [{"id": "uuid", "type": "image", "url": "url1"}, ...]
UPDATE posts 
SET media_items = (
    SELECT jsonb_agg(
        jsonb_build_object(
            'id', gen_random_uuid()::text,
            'type', CASE 
                WHEN url LIKE '%.mp4' OR url LIKE '%.webm' OR url LIKE '%.mov' THEN 'video'
                ELSE 'image'
            END,
            'url', url
        )
    )
    FROM jsonb_array_elements_text(media_urls) AS url
)
WHERE media_urls IS NOT NULL AND jsonb_array_length(media_urls) > 0;

-- Drop old media_urls column (optional - keep for rollback)
-- ALTER TABLE posts DROP COLUMN IF EXISTS media_urls;

-- ============================================================================
-- STEP 5: Update tags column type to JSONB (if it's TEXT[])
-- ============================================================================

-- Check if tags is TEXT[] and convert to JSONB
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'posts' 
        AND column_name = 'tags' 
        AND data_type = 'ARRAY'
    ) THEN
        -- Create temporary column
        ALTER TABLE posts ADD COLUMN tags_temp JSONB;
        
        -- Convert TEXT[] to JSONB
        UPDATE posts 
        SET tags_temp = to_jsonb(tags)
        WHERE tags IS NOT NULL;
        
        -- Drop old column and rename
        ALTER TABLE posts DROP COLUMN tags;
        ALTER TABLE posts RENAME COLUMN tags_temp TO tags;
    END IF;
END $$;

-- Set default for tags if NULL
UPDATE posts SET tags = '[]'::jsonb WHERE tags IS NULL;

-- ============================================================================
-- STEP 6: Add indexes for performance
-- ============================================================================

-- Index on user_id (if not exists)
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);

-- Index on group_id (if not exists)
CREATE INDEX IF NOT EXISTS idx_posts_group_id ON posts(group_id);

-- Index on created_at (if not exists)
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

-- Index on visibility for feed queries
CREATE INDEX IF NOT EXISTS idx_posts_visibility ON posts(visibility);

-- Index on type
CREATE INDEX IF NOT EXISTS idx_posts_type ON posts(type);

-- GIN index for JSONB tags (for searching)
CREATE INDEX IF NOT EXISTS idx_posts_tags ON posts USING GIN (tags);

-- GIN index for JSONB mentions (for searching)
CREATE INDEX IF NOT EXISTS idx_posts_mentions ON posts USING GIN (mentions);

-- ============================================================================
-- STEP 7: Add constraints
-- ============================================================================

-- Ensure content is not null
ALTER TABLE posts ALTER COLUMN content SET NOT NULL;

-- Ensure visibility has valid values
ALTER TABLE posts ADD CONSTRAINT check_visibility 
    CHECK (visibility IN ('public', 'followers', 'me'));

-- Ensure type has valid values
ALTER TABLE posts ADD CONSTRAINT check_post_type 
    CHECK (type IN ('text', 'image', 'video', 'poll', 'repost'));

-- ============================================================================
-- STEP 8: Update existing data
-- ============================================================================

-- Set default visibility for existing posts
UPDATE posts SET visibility = 'public' WHERE visibility IS NULL;

-- Set edited to false for existing posts
UPDATE posts SET edited = FALSE WHERE edited IS NULL;

-- Set default empty arrays for JSONB columns
UPDATE posts SET tags = '[]'::jsonb WHERE tags IS NULL;
UPDATE posts SET mentions = '[]'::jsonb WHERE mentions IS NULL;

-- ============================================================================
-- STEP 9: Verify migration
-- ============================================================================

-- Check column structure
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'posts'
ORDER BY ordinal_position;

-- Check sample data
SELECT 
    id,
    user_id,
    content,
    type,
    tags,
    mentions,
    media_items,
    visibility,
    edited,
    poll_question,
    created_at
FROM posts 
LIMIT 5;

-- Count posts with different types
SELECT 
    type,
    COUNT(*) as count
FROM posts
GROUP BY type;

-- ============================================================================
-- STEP 10: Commit or Rollback
-- ============================================================================

-- If everything looks good, commit:
COMMIT;

-- If there are issues, rollback:
-- ROLLBACK;

-- ============================================================================
-- ROLLBACK SCRIPT (In case you need to revert)
-- ============================================================================
/*
BEGIN;

-- Restore from backup
DROP TABLE IF EXISTS posts;
ALTER TABLE posts_backup RENAME TO posts;

-- Recreate indexes
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_group_id ON posts(group_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

COMMIT;
*/

-- ============================================================================
-- POST-MIGRATION VERIFICATION QUERIES
-- ============================================================================

-- Verify all posts have required fields
SELECT 
    COUNT(*) as total_posts,
    COUNT(CASE WHEN content IS NULL THEN 1 END) as null_content,
    COUNT(CASE WHEN user_id IS NULL THEN 1 END) as null_user,
    COUNT(CASE WHEN visibility IS NULL THEN 1 END) as null_visibility,
    COUNT(CASE WHEN edited IS NULL THEN 1 END) as null_edited
FROM posts;

-- Check JSONB structure
SELECT 
    id,
    jsonb_typeof(tags) as tags_type,
    jsonb_typeof(mentions) as mentions_type,
    jsonb_typeof(media_items) as media_items_type
FROM posts 
WHERE media_items IS NOT NULL
LIMIT 5;

-- Verify media_items structure
SELECT 
    id,
    jsonb_array_length(media_items) as media_count,
    media_items
FROM posts 
WHERE media_items IS NOT NULL
LIMIT 5;

-- Check for posts with polls
SELECT 
    COUNT(*) as poll_posts
FROM posts 
WHERE poll_question IS NOT NULL;

-- ============================================================================
-- ADDITIONAL INDEXES FOR OPTIMIZATION (Optional)
-- ============================================================================

-- Composite index for user feed queries
CREATE INDEX IF NOT EXISTS idx_posts_user_created 
    ON posts(user_id, created_at DESC);

-- Composite index for group feed queries
CREATE INDEX IF NOT EXISTS idx_posts_group_created 
    ON posts(group_id, created_at DESC);

-- Index for visibility and created_at (public feed)
CREATE INDEX IF NOT EXISTS idx_posts_visibility_created 
    ON posts(visibility, created_at DESC);

-- ============================================================================
-- SAMPLE DATA INSERT (For testing)
-- ============================================================================
/*
-- Insert sample post with all features
INSERT INTO posts (
    user_id, 
    type, 
    content, 
    tags, 
    mentions, 
    media_items,
    visibility, 
    edited,
    likes_count,
    comments_count,
    shares_count,
    created_at, 
    updated_at
) VALUES (
    1, -- Replace with actual user_id
    'text',
    'Hello World! This is a test post with #tags and @mentions',
    '["test", "hello"]'::jsonb,
    '["johndoe", "janedoe"]'::jsonb,
    NULL,
    'public',
    FALSE,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Insert post with media
INSERT INTO posts (
    user_id, 
    type, 
    content, 
    tags, 
    mentions, 
    media_items,
    visibility, 
    edited,
    likes_count,
    comments_count,
    shares_count,
    created_at, 
    updated_at
) VALUES (
    1,
    'image',
    'Check out this photo!',
    '["photography", "nature"]'::jsonb,
    '[]'::jsonb,
    '[
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "type": "image",
            "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
        }
    ]'::jsonb,
    'public',
    FALSE,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Insert poll post
INSERT INTO posts (
    user_id, 
    type, 
    content, 
    tags, 
    mentions, 
    poll_question,
    visibility, 
    edited,
    likes_count,
    comments_count,
    shares_count,
    created_at, 
    updated_at
) VALUES (
    1,
    'poll',
    'What is your favorite programming language?',
    '["programming", "poll"]'::jsonb,
    '[]'::jsonb,
    'Choose your favorite',
    'public',
    FALSE,
    0,
    0,
    0,
    NOW(),
    NOW()
);
*/

-- ============================================================================
-- MAINTENANCE QUERIES
-- ============================================================================

-- Analyze tables for query optimization
ANALYZE posts;
ANALYZE reactions;
ANALYZE bookmarks;
ANALYZE poll_options;

-- Vacuum tables
VACUUM ANALYZE posts;

-- Check table size
SELECT 
    pg_size_pretty(pg_total_relation_size('posts')) as total_size,
    pg_size_pretty(pg_relation_size('posts')) as table_size,
    pg_size_pretty(pg_indexes_size('posts')) as indexes_size;

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================

-- Migration completed successfully!
-- Remember to:
-- 1. Test the application thoroughly
-- 2. Monitor database performance
-- 3. Keep the backup table for a few days
-- 4. Update your application.properties if needed
-- 5. Restart your Spring Boot application
