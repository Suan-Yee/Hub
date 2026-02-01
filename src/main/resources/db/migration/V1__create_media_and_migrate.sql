-- Create media table
CREATE TABLE IF NOT EXISTS media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(512) NOT NULL,
    type VARCHAR(20) NOT NULL,
    content_id BIGINT NOT NULL,
    CONSTRAINT fk_media_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- Migrate data from image table
INSERT INTO media (url, type, content_id)
SELECT name, 'IMAGE', content_id FROM image WHERE content_id IS NOT NULL;

-- Migrate data from video table
INSERT INTO media (url, type, content_id)
SELECT name, 'VIDEO', content_id FROM video WHERE content_id IS NOT NULL;

-- Migrate data from file table (file is MySQL reserved word)
INSERT INTO media (url, type, content_id)
SELECT name, 'FILE', content_id FROM `file` WHERE content_id IS NOT NULL;

-- Drop old tables
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS video;
DROP TABLE IF EXISTS `file`;
