-- =====================================================
-- OAuth2 Integration Migration
-- =====================================================
-- Description: Add OAuth2 provider tracking columns to users table
-- Date: 2024
-- Purpose: Support Google OAuth2 authentication alongside password-based auth
-- 
-- Changes:
--   1. Add oauth_provider column to store provider name (e.g., 'google', 'github')
--   2. Add oauth_provider_id column to store unique user ID from OAuth provider
--
-- Usage:
--   Execute this migration on your database before deploying OAuth2 feature
--   Compatible with both development and production environments
-- =====================================================

-- Add oauth_provider column
-- Stores the OAuth2 provider name (e.g., 'google', 'github', 'facebook')
-- NULL for users who registered with password-based authentication
ALTER TABLE users 
ADD COLUMN oauth_provider VARCHAR(20) NULL;

-- Add oauth_provider_id column
-- Stores the unique user identifier from the OAuth2 provider (e.g., Google's 'sub' claim)
-- NULL for users who registered with password-based authentication
ALTER TABLE users 
ADD COLUMN oauth_provider_id VARCHAR(100) NULL;

-- Add comment to document the columns
COMMENT ON COLUMN users.oauth_provider IS 'OAuth2 provider name (e.g., google, github). NULL for password-based users.';
COMMENT ON COLUMN users.oauth_provider_id IS 'Unique user ID from OAuth2 provider. NULL for password-based users.';

-- Optional: Create index for faster OAuth2 user lookups
-- This improves performance when finding users by their OAuth provider credentials
CREATE INDEX idx_users_oauth_provider ON users (oauth_provider, oauth_provider_id);

-- Verify migration
SELECT 
    table_name, 
    column_name, 
    data_type, 
    character_maximum_length, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'users' 
  AND column_name IN ('oauth_provider', 'oauth_provider_id')
ORDER BY ordinal_position;

-- Expected output:
-- table_name | column_name       | data_type         | character_maximum_length | is_nullable
-- -----------|-------------------|-------------------|--------------------------|------------
-- users      | oauth_provider    | character varying | 20                       | YES
-- users      | oauth_provider_id | character varying | 100                      | YES
