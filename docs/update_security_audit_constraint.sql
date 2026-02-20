-- =============================================
-- Security Audit Logs - Update Constraint
-- =============================================
-- Description: Updates check constraint to include business events
-- Date: February 20, 2026
-- =============================================

-- Drop the old constraint
ALTER TABLE security_audit_logs 
DROP CONSTRAINT IF EXISTS chk_event_type;

-- Add updated constraint with business events
ALTER TABLE security_audit_logs
ADD CONSTRAINT chk_event_type CHECK (
    event_type IN (
        -- Authentication events
        'LOGIN_SUCCESS',
        'LOGIN_FAILURE',
        'REGISTRATION',
        'LOGOUT',
        'ACCESS_DENIED',
        'TOKEN_EXPIRED',
        'TOKEN_INVALID',
        'PASSWORD_CHANGE',
        'OAUTH2_LOGIN_SUCCESS',
        'OAUTH2_LOGIN_FAILURE',
        
        -- Business events
        'ORDER_CREATED',
        'ORDER_UPDATED',
        'ORDER_CANCELLED',
        'ORDER_STATUS_CHANGED',
        'PAYMENT_INITIATED',
        'PAYMENT_COMPLETED',
        'PAYMENT_FAILED'
    )
);

-- Verify the constraint was applied
SELECT conname, pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conname = 'chk_event_type'
AND conrelid = 'security_audit_logs'::regclass;
