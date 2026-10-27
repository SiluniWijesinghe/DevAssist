CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- These CREATE statements also support a brand-new database. Existing tables are preserved.
CREATE TABLE IF NOT EXISTS repositories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    default_branch VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    owner_id BIGINT
);

CREATE TABLE IF NOT EXISTS repository_files (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    extension VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    content TEXT,
    repository_id BIGINT NOT NULL REFERENCES repositories(id)
);

-- Existing repositories are retained but remain unowned until explicitly reassigned.
-- They cannot be accessed through authenticated endpoints while owner_id is NULL.
ALTER TABLE repositories ADD COLUMN IF NOT EXISTS owner_id BIGINT;
ALTER TABLE repositories DROP CONSTRAINT IF EXISTS repositories_url_key;
DO $$ DECLARE constraint_name TEXT; BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        WHERE t.relname = 'repositories' AND c.contype = 'u'
          AND pg_get_constraintdef(c.oid) = 'UNIQUE (url)'
    LOOP
        EXECUTE format('ALTER TABLE repositories DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_repositories_owner' AND conrelid = 'repositories'::regclass) THEN
        ALTER TABLE repositories ADD CONSTRAINT fk_repositories_owner
            FOREIGN KEY (owner_id) REFERENCES users(id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_repositories_owner_id ON repositories(owner_id);
