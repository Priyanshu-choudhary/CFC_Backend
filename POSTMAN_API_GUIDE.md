# Spring Boot API - Postman Testing Guide

## Base URL
```
http://localhost:9090
```

---

## 📝 Public Endpoints (No Authentication Required)

### 1️⃣ **Create User / Signup**

**Endpoint:** `POST /Public/Create-User`

**URL:** `http://localhost:9090/Public/Create-User`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "john_doe",
  "email": "john@example.com",
  "password": "mySecurePassword123",
  "roles": ["USER"],
  "profileImg": "https://example.com/profile.jpg"
}
```

**Create ADMIN User:**
```json
{
  "name": "admin",
  "email": "admin@example.com",
  "password": "admin123",
  "roles": ["ADMIN", "USER"],
  "profileImg": "https://example.com/admin.jpg"
}
```

**Response (201 Created):**
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "name": "john_doe",
  "email": "john@example.com",
  "password": "$2a$10$encoded_password_hash",
  "roles": ["USER"],
  "profileImg": "https://example.com/profile.jpg",
  "posts": [],
  "courses": [],
  "contests": [],
  "lectures": []
}
```

**Error Responses:**
- `409 CONFLICT` - User already exists
- `400 BAD REQUEST` - Invalid data

---

### 2️⃣ **Get User by Username**

**Endpoint:** `GET /Public/showUser/{username}`

**URL:** `http://localhost:9090/Public/showUser/john_doe`

**Headers:** None required

**Response (200 OK):**
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "name": "john_doe",
  "email": "john@example.com",
  "profileImg": "https://example.com/profile.jpg",
  "rating": 0,
  "postCount": 0,
  "roles": ["USER"]
}
```

---

### 3️⃣ **Get User by ID**

**Endpoint:** `GET /Public/{id}`

**URL:** `http://localhost:9090/Public/65a1b2c3d4e5f6g7h8i9j0k1`

**Headers:** None required

---

### 4️⃣ **Get All Users (Paginated)**

**Endpoint:** `GET /Public/getAllUsers`

**URL:** `http://localhost:9090/Public/getAllUsers?page=0&size=10`

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Items per page (default: 10)

---

### 5️⃣ **Health Check**

**Endpoint:** `GET /Public/HealthCheck`

**URL:** `http://localhost:9090/Public/HealthCheck`

**Response:**
```json
{
  "server_status": "running",
  "database_status": "up",
  "cpu_load": 1.5,
  "heap_memory_used": "512 MB",
  "disk_free_space": "100 GB"
}
```

---

## 🔒 Protected Endpoints (Authentication Required)

### 6️⃣ **Login**

**Endpoint:** `POST /users/login`

**URL:** `http://localhost:9090/users/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "john_doe",
  "password": "mySecurePassword123"
}
```

**Response (200 OK):**
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "name": "john_doe",
  "email": "john@example.com",
  "roles": ["USER"],
  "profileImg": "https://example.com/profile.jpg"
}
```

**Error Response:**
- `401 UNAUTHORIZED` - Invalid credentials

---

### 7️⃣ **Get Current User (Authenticated)**

**Endpoint:** `GET /users/getUser`

**URL:** `http://localhost:9090/users/getUser`

**Authentication:** HTTP Basic Auth
- Username: `john_doe`
- Password: `mySecurePassword123`

**In Postman:**
1. Go to **Authorization** tab
2. Select **Type:** `Basic Auth`
3. Enter **Username** and **Password**

**Response (200 OK):**
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "name": "john_doe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

---

### 8️⃣ **Update User Profile**

**Endpoint:** `PUT /users`

**URL:** `http://localhost:9090/users`

**Authentication:** HTTP Basic Auth (required)

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "john_doe_updated",
  "email": "john_updated@example.com",
  "password": "newPassword123",
  "collage": "MIT",
  "branch": "Computer Science",
  "year": "2024",
  "skills": "Java, Spring Boot, React",
  "profileImg": "https://example.com/new_profile.jpg",
  "roles": ["USER"]
}
```

**Response:** `204 NO CONTENT`

---

### 9️⃣ **Delete User by ID**

**Endpoint:** `DELETE /Public/user/id/{id}`

**URL:** `http://localhost:9090/Public/user/id/65a1b2c3d4e5f6g7h8i9j0k1`

**Response:** `200 OK`

---

## 📋 How to Test in Postman

### Step 1: Create a New User (Signup)
1. Open Postman
2. Create a new request: `POST`
3. Enter URL: `http://localhost:9090/Public/Create-User`
4. Go to **Headers** tab, add:
   - Key: `Content-Type`
   - Value: `application/json`
5. Go to **Body** tab
6. Select **raw** and **JSON**
7. Paste the JSON body (see example above)
8. Click **Send**

### Step 2: Login
1. Create a new request: `POST`
2. Enter URL: `http://localhost:9090/users/login`
3. Add header: `Content-Type: application/json`
4. Body:
   ```json
   {
     "name": "john_doe",
     "password": "mySecurePassword123"
   }
   ```
5. Click **Send**

### Step 3: Access Protected Endpoints
1. Create a new request: `GET`
2. Enter URL: `http://localhost:9090/users/getUser`
3. Go to **Authorization** tab
4. Select **Type:** `Basic Auth`
5. Username: `john_doe`
6. Password: `mySecurePassword123`
7. Click **Send**

---

## 🔑 Creating Different User Roles

### Regular User:
```json
{
  "name": "regular_user",
  "email": "user@example.com",
  "password": "user123",
  "roles": ["USER"]
}
```

### Admin User:
```json
{
  "name": "admin",
  "email": "admin@example.com",
  "password": "admin123",
  "roles": ["ADMIN", "USER"]
}
```

### Moderator User:
```json
{
  "name": "moderator",
  "email": "mod@example.com",
  "password": "mod123",
  "roles": ["MODERATOR", "USER"]
}
```

---

## ⚠️ Important Notes

1. **Password Encoding**: Passwords are automatically encrypted using BCrypt when creating users
2. **Authentication**: Most `/users/*` endpoints require Basic Authentication
3. **Public Access**: All `/Public/*` endpoints don't require authentication
4. **CORS**: Configured for `http://localhost:5173` and production domains
5. **User Uniqueness**: Username must be unique (409 error if duplicate)

---

## 🧪 Sample Postman Collection

Save this as `CFC_API_Collection.json`:

```json
{
  "info": {
    "name": "CFC API Collection",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create User (Signup)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"testuser\",\n  \"email\": \"test@example.com\",\n  \"password\": \"test123\",\n  \"roles\": [\"USER\"]\n}"
        },
        "url": {
          "raw": "http://localhost:9090/Public/Create-User",
          "protocol": "http",
          "host": ["localhost"],
          "port": "9090",
          "path": ["Public", "Create-User"]
        }
      }
    },
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"testuser\",\n  \"password\": \"test123\"\n}"
        },
        "url": {
          "raw": "http://localhost:9090/users/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "9090",
          "path": ["users", "login"]
        }
      }
    }
  ]
}
```

---

## 📞 Support

If you encounter any issues:
- Check if the Spring Boot server is running on port 9090
- Verify MongoDB is running
- Check the application logs for errors
