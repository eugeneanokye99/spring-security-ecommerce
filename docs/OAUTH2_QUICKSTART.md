# OAuth2 Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide will help you set up and test Google OAuth2 authentication quickly.

---

## ✅ Prerequisites Completed

- ✅ Backend OAuth2 code implemented
- ✅ Frontend OAuth2 components created
- ✅ jwt-decode package installed
- ✅ Documentation created

---

## 🎯 Steps to Complete (In Order)

### Step 1: Run Database Migration (2 minutes)

```bash
# Open PostgreSQL terminal
psql -U postgres -d shopjoy

# Run migration script
\i docs/oauth2_migration.sql

# Verify columns added
\d users

# Exit psql
\q
```

**Expected Output**: You should see `oauth_provider` and `oauth_provider_id` columns in the users table.

---

### Step 2: Create Google Cloud Project (5 minutes)

1. Open [Google Cloud Console](https://console.cloud.google.com/)
2. Click **project dropdown** → **New Project**
3. Name: `ShopJoy E-Commerce`
4. Click **Create**

---

### Step 3: Configure OAuth Consent Screen (3 minutes)

1. Navigate to **APIs & Services** → **OAuth consent screen**
2. User Type: **External** → **Create**
3. Fill in:
   - **App name**: ShopJoy E-Commerce
   - **User support email**: Your email
   - **Developer contact**: Your email
4. Click **Save and Continue**
5. **Scopes**: Click **Add or Remove Scopes**
   - Select: `email`, `profile`
   - Click **Update** → **Save and Continue**
6. **Test users**: Click **Add Users**
   - Add your Google email address
   - Click **Add** → **Save and Continue**
7. Click **Back to Dashboard**

---

### Step 4: Create OAuth2 Credentials (2 minutes)

1. Navigate to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. Application type: **Web application**
4. Name: `ShopJoy Backend`
5. **Authorized redirect URIs** → **Add URI**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
6. Click **Create**
7. **Copy** both:
   - Client ID (looks like: `123456789-abc...apps.googleusercontent.com`)
   - Client Secret (looks like: `GOCSPX-ABC123...`)

---

### Step 5: Set Environment Variables (1 minute)

**Windows PowerShell**:
```powershell
$env:GOOGLE_CLIENT_ID="PASTE_YOUR_CLIENT_ID_HERE"
$env:GOOGLE_CLIENT_SECRET="PASTE_YOUR_CLIENT_SECRET_HERE"
```

**Alternative: Create .env file** (Recommended)
```bash
# Copy template
copy .env.example .env

# Edit .env file and add:
GOOGLE_CLIENT_ID=your-actual-client-id
GOOGLE_CLIENT_SECRET=your-actual-client-secret
```

---

### Step 6: Start the Application (2 minutes)

**Terminal 1 (Backend)**:
```bash
mvn spring-boot:run
```

Wait for message: `Started ShopJoyApplication in XX seconds`

**Terminal 2 (Frontend)**:
```bash
cd frontend
npm run dev
```

Wait for message: `Local: http://localhost:5173/`

---

### Step 7: Test OAuth2 Login (1 minute)

1. Open browser: `http://localhost:5173/login`
2. You should see:
   - Traditional login form (username/password)
   - **"Or continue with"** divider
   - **"Sign in with Google"** button with Google logo
3. Click **"Sign in with Google"**
4. You'll be redirected to Google
5. Sign in with your Google account (use the test user you added)
6. Grant permissions (email, profile)
7. You should be redirected back to your app
8. You should see a success screen, then automatic redirect to dashboard

---

## ✅ Verification Checklist

After testing, verify:

- [ ] Redirected to Google consent screen
- [ ] Signed in with Google successfully
- [ ] Redirected to dashboard (not login page)
- [ ] Profile information displayed correctly
- [ ] Can access protected resources

**Check Database**:
```sql
SELECT user_id, username, email, first_name, last_name, oauth_provider, oauth_provider_id 
FROM users 
WHERE oauth_provider = 'google';
```

You should see your Google account information.

**Check Browser**:
- Open Developer Tools → Application → Local Storage
- Should see `token` and `user` stored

---

## 🐛 Troubleshooting

### Error: "redirect_uri_mismatch"
- Double-check redirect URI in Google Console
- Must be exactly: `http://localhost:8080/login/oauth2/code/google`
- No typos, correct port, no trailing slash

### Error: "Access blocked"
- Add your email to **Test users** in OAuth consent screen
- Wait a few minutes after adding

### Error: "invalid_client"
- Verify environment variables are set correctly
- Restart backend after setting variables
- Check for extra spaces in Client ID/Secret

### Not redirected after Google login
- Check backend logs for errors
- Verify database migration was successful
- Check `application.yml` OAuth2 configuration

### Token not found in frontend
- Check browser console for errors
- Verify `/oauth2/callback` route exists in App.jsx
- Check OAuth2Callback component is imported

---

## 📖 Full Documentation

For detailed information, see:

- **Setup Guide**: [docs/GOOGLE_OAUTH2_SETUP.md](GOOGLE_OAUTH2_SETUP.md)
- **Implementation Summary**: [docs/OAUTH2_IMPLEMENTATION_SUMMARY.md](OAUTH2_IMPLEMENTATION_SUMMARY.md)
- **Database Migration**: [docs/oauth2_migration.sql](oauth2_migration.sql)

---

## 🎉 Success!

Once you can log in with Google and see your dashboard, OAuth2 is working correctly!

Next steps:
- Test logout functionality
- Test traditional login still works
- Try logging in with different Google accounts
- Deploy to production (see full documentation)

---

**Need Help?**
- Check application logs: `logs/spring.log`
- Review [docs/GOOGLE_OAUTH2_SETUP.md](GOOGLE_OAUTH2_SETUP.md) troubleshooting section
- Verify all environment variables are set
