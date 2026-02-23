-- Migration: Make password_hash nullable for OAuth2 users
-- Date: 2026-02-23
-- Description: Updates the users table to allow null password_hash for OAuth2 authenticated users
--              and sets existing OAuth2 users' passwords to null
-- Database: PostgreSQL

-- Step 1: Modify the password_hash column to allow NULL values
ALTER TABLE users 
ALTER COLUMN password_hash DROP NOT NULL;

-- Step 2: Update existing OAuth2 users to have NULL password_hash
-- This prevents OAuth2 users from logging in with regular username/password authentication
UPDATE users 
SET password_hash = NULL 
WHERE oauth_provider IS NOT NULL 
  AND oauth_provider_id IS NOT NULL;

-- Step 3: Verify the changes
-- Check OAuth2 users now have NULL passwords
SELECT 
    user_id,
    username,
    email,
    oauth_provider,
    CASE 
        WHEN password_hash IS NULL THEN 'NULL (OAuth2 User)'
        ELSE 'SET (Regular User)'
    END AS password_status,
    created_at
FROM users
WHERE oauth_provider IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;

-- Step 4: Verify regular users still have passwords
SELECT 
    COUNT(*) as regular_users_with_passwords
FROM users
WHERE oauth_provider IS NULL 
  AND password_hash IS NOT NULL;

-- Step 5: Check for any orphaned records (should be 0)
SELECT 
    COUNT(*) as orphaned_records
FROM users
WHERE oauth_provider IS NULL 
  AND password_hash IS NULL;

-- Note: If there are any orphaned records (oauth_provider is NULL but password_hash is also NULL),
-- these users won't be able to login with either method and should be investigated.

-- Rollback script (if needed):
-- ALTER TABLE users 
-- ALTER COLUMN password_hash SET NOT NULL;
--
-- UPDATE users 
-- SET password_hash = '$2a$10$PLACEHOLDER_HASH_IF_NEEDED' 
-- WHERE password_hash IS NULL;
