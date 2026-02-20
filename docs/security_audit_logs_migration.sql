-- =============================================
-- Security Audit Logs Migration Script
-- =============================================
-- Description: Creates table for tracking security events
-- Date: February 20, 2026
-- =============================================

-- Create security_audit_logs table
CREATE TABLE IF NOT EXISTS security_audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100),
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    success BOOLEAN,
    CONSTRAINT chk_event_type CHECK (
        event_type IN (
            'LOGIN_SUCCESS',
            'LOGIN_FAILURE',
            'REGISTRATION',
            'LOGOUT',
            'ACCESS_DENIED',
            'TOKEN_EXPIRED',
            'TOKEN_INVALID',
            'PASSWORD_CHANGE',
            'OAUTH2_LOGIN_SUCCESS',
            'OAUTH2_LOGIN_FAILURE'
        )
    )
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_username ON security_audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_event_type ON security_audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_timestamp ON security_audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_username_timestamp ON security_audit_logs(username, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_event_type_timestamp ON security_audit_logs(event_type, timestamp DESC);

-- Add comment to table
COMMENT ON TABLE security_audit_logs IS 'Stores security audit trail for authentication and authorization events';

-- Add comments to columns
COMMENT ON COLUMN security_audit_logs.log_id IS 'Primary key - auto-incrementing log identifier';
COMMENT ON COLUMN security_audit_logs.username IS 'Username associated with the event (null for anonymous events)';
COMMENT ON COLUMN security_audit_logs.event_type IS 'Type of security event';
COMMENT ON COLUMN security_audit_logs.ip_address IS 'Client IP address (supports IPv4 and IPv6)';
COMMENT ON COLUMN security_audit_logs.user_agent IS 'Client user agent string from HTTP request';
COMMENT ON COLUMN security_audit_logs.timestamp IS 'Timestamp when the event occurred';
COMMENT ON COLUMN security_audit_logs.details IS 'Additional details about the event in JSON or text format';
COMMENT ON COLUMN security_audit_logs.success IS 'Whether the security event was successful';

-- Grant permissions (adjust as needed for your database user)
-- GRANT SELECT, INSERT ON security_audit_logs TO shopjoy_app_user;
-- GRANT USAGE, SELECT ON SEQUENCE security_audit_logs_log_id_seq TO shopjoy_app_user;

-- Verification query
-- SELECT COUNT(*) FROM security_audit_logs;
