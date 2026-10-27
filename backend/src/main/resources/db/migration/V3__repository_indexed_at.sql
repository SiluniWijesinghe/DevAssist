ALTER TABLE repositories ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMP;
UPDATE repositories SET indexed_at = created_at WHERE indexed_at IS NULL;