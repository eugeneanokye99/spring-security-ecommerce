# Google OAuth2 Setup Guide

Complete step-by-step guide to configure Google OAuth2 authentication for the E-Commerce application.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Google Cloud Console Setup](#google-cloud-console-setup)
3. [OAuth2 Credentials Configuration](#oauth2-credentials-configuration)
4. [Backend Configuration](#backend-configuration)
5. [Testing OAuth2 Flow](#testing-oauth2-flow)
6. [Troubleshooting](#troubleshooting)
7. [Production Deployment](#production-deployment)

---

## Prerequisites

Before starting, ensure you have:
- A Google account
- Access to [Google Cloud Console](https://console.cloud.google.com/)
- Application running locally on `localhost:8080` (backend) and `localhost:5173` (frontend)
- Database migration completed (see `docs/oauth2_migration.sql`)

---

## Google Cloud Console Setup

### Step 1: Create a New Project

1. Navigate to [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown (top-left, next to "Google Cloud")
3. Click **"New Project"**
4. Enter project details:
   - **Project Name**: `ShopJoy E-Commerce` (or your preferred name)
   - **Organization**: Select if applicable (optional)
5. Click **"Create"**
6. Wait for project creation to complete (~30 seconds)
7. Select the newly created project from the dropdown

### Step 2: Enable Required APIs

1. In the left sidebar, navigate to **"APIs & Services" → "Library"**
2. Search for and enable the following APIs:
   - **Google+ API** (or **People API** if Google+ is deprecated)
   - **Google OAuth2 API** (usually enabled by default)

3. For each API:
   - Click on the API name
   - Click **"Enable"**
   - Wait for activation

### Step 3: Configure OAuth Consent Screen

This screen is shown to users when they authenticate.

1. Navigate to **"APIs & Services" → "OAuth consent screen"**
2. Select **User Type**:
   - **Internal**: Only for Google Workspace users in your organization
   - **External**: For public applications (recommended for e-commerce)
3. Click **"Create"**

#### Configure App Information

**Page 1: App Information**
- **App name**: `ShopJoy E-Commerce` (your app name)
- **User support email**: Your email address
- **App logo**: Upload logo (optional, recommended for production)
- **Application home page**: `http://localhost:5173` (development) or your domain (production)
- **Application privacy policy link**: Your privacy policy URL (required for production)
- **Application terms of service link**: Your TOS URL (optional)
- **Authorized domains**: 
  - Add `localhost` for development
  - Add your production domain (e.g., `shopjoy.com`)
- **Developer contact information**: Your email address
- Click **"Save and Continue"**

**Page 2: Scopes**
- Click **"Add or Remove Scopes"**
- Select the following scopes:
  - ✅ `email` - View your email address
  - ✅ `profile` - View your basic profile info
  - ✅ `openid` - Authenticate using OpenID Connect
- Click **"Update"**
- Click **"Save and Continue"**

**Page 3: Test Users** (for External apps in testing mode)
- Click **"Add Users"**
- Enter email addresses of users who can test OAuth2 login
- Click **"Add"**
- Click **"Save and Continue"**

**Page 4: Summary**
- Review your configuration
- Click **"Back to Dashboard"**

---

## OAuth2 Credentials Configuration

### Step 1: Create OAuth2 Client ID

1. Navigate to **"APIs & Services" → "Credentials"**
2. Click **"Create Credentials" → "OAuth client ID"**
3. Select **Application type**: `Web application`
4. Configure OAuth client:

   **Name**: `ShopJoy Backend OAuth2 Client`

   **Authorized JavaScript origins**:
   - Development: `http://localhost:8080`
   - Production: `https://yourdomain.com`

   **Authorized redirect URIs**:
   - Development: `http://localhost:8080/login/oauth2/code/google`
   - Production: `https://yourdomain.com/login/oauth2/code/google`

5. Click **"Create"**

### Step 2: Save Credentials

After creation, a modal will display your credentials:

```
Client ID: 123456789-abcdefghijklmnop.apps.googleusercontent.com
Client Secret: GOCSPX-ABC123def456GHI789jkl
```

**IMPORTANT**: 
- ⚠️ Copy both values immediately
- ⚠️ Store them securely (never commit to version control)
- ⚠️ You can view them later from the Credentials page if needed

---

## Backend Configuration

### Step 1: Set Environment Variables

Create or update your `.env` file (or environment configuration):

```env
# Google OAuth2 Credentials
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnop.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-ABC123def456GHI789jkl

# JWT Configuration
JWT_SECRET=your-jwt-secret-key-at-least-32-characters-long
JWT_EXPIRATION=86400000

# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/shopjoy
DB_USERNAME=postgres
DB_PASSWORD=your-database-password

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Step 2: Verify application.yml Configuration

Ensure your `application.yml` contains:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:your-google-client-id}
            client-secret: ${GOOGLE_CLIENT_SECRET:your-google-client-secret}
            scope: [email, profile]
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-name: Google
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub

security:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

### Step 3: Run Database Migration

Execute the OAuth2 migration script:

```bash
# Connect to your database
psql -U postgres -d shopjoy

# Run migration
\i docs/oauth2_migration.sql

# Verify columns were added
\d users
```

Expected columns:
- `oauth_provider` (VARCHAR(20), nullable)
- `oauth_provider_id` (VARCHAR(100), nullable)

---

## Testing OAuth2 Flow

### Step 1: Start the Application

**Backend**:
```bash
# Set environment variables (Windows PowerShell)
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"

# Start Spring Boot application
mvn spring-boot:run
```

**Frontend**:
```bash
cd frontend
npm run dev
```

### Step 2: Test OAuth2 Login

1. Navigate to `http://localhost:5173/login`
2. Click **"Sign in with Google"** button
3. You should be redirected to Google's consent screen
4. Log in with a Google account (use test user if in testing mode)
5. Grant requested permissions (email, profile)
6. You should be redirected back to your application
7. Check that you're logged in (redirected to dashboard)

### Step 3: Verify Database

Check that the user was created:

```sql
SELECT 
    user_id, 
    username, 
    email, 
    first_name, 
    last_name, 
    oauth_provider, 
    oauth_provider_id, 
    user_type
FROM users
WHERE oauth_provider = 'google';
```

Expected result:
- User exists with Google email
- `oauth_provider` = 'google'
- `oauth_provider_id` = Google user ID (`sub` claim)
- `password_hash` = 'OAUTH2_USER_NO_PASSWORD'
- `user_type` = 'CUSTOMER'

### Step 4: Check Logs

Review application logs for OAuth2 events:

```
2024-XX-XX INFO  OAuth2LoginSuccessHandler - OAuth2 login successful for user: user@gmail.com
2024-XX-XX INFO  OAuth2LoginSuccessHandler - Creating new OAuth2 user: user@gmail.com
2024-XX-XX INFO  OAuth2LoginSuccessHandler - User created successfully with ID: 123
2024-XX-XX INFO  OAuth2LoginSuccessHandler - JWT token generated for user: user
2024-XX-XX INFO  OAuth2LoginSuccessHandler - Redirecting to frontend: http://localhost:5173/oauth2/callback?token=...
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: "Error 400: redirect_uri_mismatch"

**Cause**: Redirect URI in Google Console doesn't match application configuration.

**Solution**:
1. Check Google Console → Credentials → Your OAuth Client
2. Verify **Authorized redirect URIs** contains:
   - `http://localhost:8080/login/oauth2/code/google` (development)
3. Ensure URL is exact (no trailing slashes, correct port)
4. Save changes and wait ~5 minutes for propagation

#### Issue 2: "Access blocked: This app's request is invalid"

**Cause**: OAuth consent screen not configured properly.

**Solution**:
1. Navigate to **OAuth consent screen**
2. Ensure **Publishing status** is "Testing" (for development)
3. Add your Google account to **Test users**
4. Verify **Scopes** include `email` and `profile`

#### Issue 3: "Error 401: invalid_client"

**Cause**: Invalid or incorrect OAuth2 credentials.

**Solution**:
1. Verify `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are set correctly
2. Check for extra spaces or newlines in environment variables
3. Re-download credentials from Google Console if unsure
4. Restart application after changing environment variables

#### Issue 4: User redirected to `/login?error=oauth2_failed`

**Cause**: Backend OAuth2 processing failed.

**Solution**:
1. Check application logs for detailed error messages
2. Common causes:
   - Database connection issues
   - Missing email in OAuth2 response
   - JWT token generation failure
3. Verify database migration was applied correctly
4. Check `OAuth2LoginSuccessHandler` logs

#### Issue 5: "Email not provided by OAuth2 provider"

**Cause**: Email scope not granted or not included in OAuth2 response.

**Solution**:
1. Verify OAuth consent screen includes `email` scope
2. Ensure user granted email permission during consent
3. Check Google API console for scope configuration
4. Re-authenticate and explicitly grant email permission

#### Issue 6: Token in URL but login not completing

**Cause**: Frontend token processing issue.

**Solution**:
1. Check browser console for JavaScript errors
2. Verify `OAuth2Callback` component is rendering
3. Check `AuthContext` for `loginWithOAuth2` function
4. Verify JWT token format with [jwt.io](https://jwt.io)
5. Check browser localStorage for `user` and `token`

---

## Production Deployment

### Pre-Deployment Checklist

- [ ] Update OAuth consent screen with production domain
- [ ] Add production domain to **Authorized domains**
- [ ] Update **Authorized JavaScript origins** with production URL
- [ ] Update **Authorized redirect URIs** with production URL
- [ ] Set environment variables on production server
- [ ] Update `CORS_ALLOWED_ORIGINS` with production frontend URL
- [ ] Run database migration on production database
- [ ] Submit app for Google verification (if requesting sensitive scopes)
- [ ] Publish OAuth consent screen (move from "Testing" to "In Production")
- [ ] Configure privacy policy and terms of service URLs
- [ ] Set up monitoring and logging for OAuth2 events
- [ ] Test OAuth2 flow on production environment

### Production OAuth2 Configuration

**Google Cloud Console**:
```
Authorized JavaScript origins:
  - https://api.shopjoy.com

Authorized redirect URIs:
  - https://api.shopjoy.com/login/oauth2/code/google
```

**application.yml** (production profile):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            redirect-uri: "https://api.shopjoy.com/login/oauth2/code/{registrationId}"
```

**Environment Variables**:
```env
GOOGLE_CLIENT_ID=production-client-id
GOOGLE_CLIENT_SECRET=production-client-secret
CORS_ALLOWED_ORIGINS=https://www.shopjoy.com,https://shopjoy.com
```

### Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for all sensitive configuration
3. **Rotate secrets periodically** (client secret, JWT secret)
4. **Enable HTTPS** for production (required by Google OAuth2)
5. **Implement rate limiting** on OAuth2 endpoints
6. **Monitor OAuth2 login attempts** for suspicious activity
7. **Log all OAuth2 events** for audit trail
8. **Keep dependencies updated** (Spring Security, OAuth2 client)
9. **Validate JWT tokens** on every API request
10. **Set appropriate CORS policies** for production

---

## Additional Resources

- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 Login Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OAuth2 Debugging Tool](https://oauthdebugger.com/)

---

## Support

If you encounter issues not covered in this guide:

1. Check application logs (`logs/spring.log`)
2. Review Google Cloud Console **Quota** page for API limits
3. Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/google-oauth2)
4. Contact development team

---

**Last Updated**: 2024  
**Version**: 1.0  
**Author**: ShopJoy Development Team
