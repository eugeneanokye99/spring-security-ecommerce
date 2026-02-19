# JWT Authentication Testing Guide

## Test Endpoints with Postman

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Register a New User

**Endpoint:** `POST /auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "email": "testcustomer@example.com",
  "password": "TestPassword123",
  "firstName": "Test",
  "lastName": "Customer",
  "phone": "1234567890"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "testcustomer",
    "email": "testcustomer@example.com",
    "firstName": "Test",
    "lastName": "Customer",
    "phone": "1234567890",
    "userType": "CUSTOMER",
    "createdAt": "2026-02-19T10:30:00",
    "updatedAt": "2026-02-19T10:30:00"
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 2. Login and Receive JWT Token

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "password": "TestPassword123"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQ1VTVE9NRVIiLCJzdWIiOiJ0ZXN0Y3VzdG9tZXIiLCJpYXQiOjE3MDg3MDQwMDAsImV4cCI6MTcwODc5MDQwMH0.abc123...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 3. Test Invalid Login Credentials

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "password": "WrongPassword"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid username or password",
  "data": null,
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 4. Test with Admin Account

**Step 1: Check if admin exists (should be auto-created)**

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 5. Using JWT Token in Authenticated Requests

Once you have a JWT token, include it in the Authorization header for protected endpoints:

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQ1VTVE9NRVIiLCJzdWIiOiJ0ZXN0Y3VzdG9tZXIiLCJpYXQiOjE3MDg3MDQwMDAsImV4cCI6MTcwODc5MDQwMH0.abc123...
Content-Type: application/json
```

---

## Token Validation Points

### Valid Token Characteristics:
- ✅ Contains correct username
- ✅ Not expired (valid for 24 hours)
- ✅ Properly signed with HMAC SHA-256
- ✅ Contains roles claim

### Token Expiration:
- Default expiration: **24 hours** (86400000 milliseconds)
- Can be configured in `application.yml` under `jwt.expiration`

---

## JWT Token Structure

### Decoded Token Example:

**Header:**
```json
{
  "alg": "HS256"
}
```

**Payload:**
```json
{
  "roles": "ROLE_CUSTOMER",
  "sub": "testcustomer",
  "iat": 1708704000,
  "exp": 1708790400
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

---

## Testing JWT Validation

You can decode and verify JWT tokens at: https://jwt.io

**Secret Key (for development):**
```
404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

⚠️ **Note:** Never use this secret in production. Set environment variable `JWT_SECRET` with a secure random key.

---

## Postman Collection Variables

Create these variables in your Postman environment:

| Variable      | Value                          |
|---------------|--------------------------------|
| `base_url`    | `http://localhost:8080/api/v1` |
| `jwt_token`   | (auto-filled from login)       |

---

## Testing Workflow

1. **Register** a new user
2. **Login** with the credentials
3. **Copy** the JWT token from the response
4. **Set** the token in Postman environment variable or Authorization header
5. **Make** authenticated requests to protected endpoints

---

## Common Issues

### 401 Unauthorized
- Check if token is included in Authorization header
- Verify token format: `Bearer <token>`
- Ensure token hasn't expired

### 403 Forbidden
- Token is valid but user doesn't have required role
- Check if endpoint requires ROLE_ADMIN but user is ROLE_CUSTOMER

### Invalid Token
- Token may be expired
- Token signature verification failed
- Token structure is malformed
