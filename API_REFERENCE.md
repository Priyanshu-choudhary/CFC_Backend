# 🚀 CFC Backend - Complete API Reference

**Base URL:** `http://localhost:9090`

---

## 📑 Table of Contents

1. [Authentication & User Management](#authentication--user-management)
2. [Course Management](#course-management)
3. [Contest Management](#contest-management)
4. [Post/Problem Management](#postproblem-management)
5. [Lecture Management](#lecture-management)
6. [User Contest Details](#user-contest-details)
7. [Topic Skills](#topic-skills)
8. [File Upload](#file-upload)

---

## 🔄 Complete Execution Flow & Mental Model

### 🎯 Big Picture: Backend Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         USER                                  │
│  ┌──────────────┐                                            │
│  │ Basic Auth   │ (username + password)                      │
│  └──────────────┘                                            │
└─────────────────────────────────────────────────────────────┘
         │
         ├─── Content Creation (Requires Auth ✅)
         │    │
         │    ├─── 📚 COURSE
         │    │    ├─── Posts (Problems) → Reference Course
         │    │    └─── Metadata: title, description, rating
         │    │
         │    ├─── 🏆 CONTEST
         │    │    ├─── Posts (Problems) → Reference Contest
         │    │    └─── Metadata: date, rewards, eligibility
         │    │
         │    └─── 📖 LECTURE
         │         ├─── Headings & SubHeadings
         │         └─── Educational Content
         │
         ├─── Learning Structures
         │    │
         │    ├─── 🎯 TOPIC SKILLS
         │    │    └─── Hierarchical: Topic → Children → Problems
         │    │
         │    └─── 👤 USER CONTEST DETAILS
         │         └─── Track: timeTaken, endTime, results
         │
         └─── Media
              └─── 📁 FILE UPLOAD
                   └─── Images, Documents → Returns URL
```

---

## 📋 Complete Testing Flow - Step by Step

### ✅ **PHASE 1: Setup & Verification** (2 steps)

#### **Step 1: Health Check**

_Verify server is running and connected to MongoDB_

```bash
GET http://localhost:9090/Public/HealthCheck
```

**Expected Response:**

```json
{
  "server_status": "running",
  "database_status": "up",
  "cpu_load": 1.5,
  "heap_memory_used": "512 MB"
}
```

---

#### **Step 2: Create User (Signup)**

_Register a new user account_

```bash
POST http://localhost:9090/Public/Create-User
Content-Type: application/json

{
  "name": "testuser",
  "email": "test@example.com",
  "password": "test123",
  "roles": ["USER"],
  "profileImg": ""
}
```

**Expected Response (201 Created):**

```json
{
  "id": "65f1a2b3c4d5e6f7g8h9i0j1",
  "name": "testuser",
  "email": "test@example.com",
  "password": "$2a$10$...", // BCrypt hash
  "roles": ["USER"],
  "posts": [],
  "courses": [],
  "contests": []
}
```

**💡 Note:** Password is automatically encrypted with BCrypt

---

### ✅ **PHASE 2: Authentication Verification** (2 steps)

#### **Step 3: Login (Verify Credentials)**

_Test if user can authenticate_

```bash
POST http://localhost:9090/users/login
Content-Type: application/json

{
  "name": "testuser",
  "password": "test123"
}
```

**Expected Response (200 OK):**

```json
{
  "id": "65f1a2b3c4d5e6f7g8h9i0j1",
  "name": "testuser",
  "email": "test@example.com",
  "roles": ["USER"]
}
```

**❌ Wrong Password Response (401 Unauthorized):**

```json
// Empty body with 401 status
```

---

#### **Step 4: Get Current User (Protected Endpoint)**

_Verify Basic Auth works for protected routes_

```bash
GET http://localhost:9090/users/getUser
Authorization: Basic testuser:test123
```

**Expected Response (200 OK):**

```json
{
  "id": "65f1a2b3c4d5e6f7g8h9i0j1",
  "name": "testuser",
  "email": "test@example.com",
  "posts": [],
  "courses": [],
  "contests": [],
  "postCount": 0
}
```

**💡 From now on, all protected routes need Basic Auth header**

---

### ✅ **PHASE 3: Course Management Flow** (4 steps)

#### **Step 5: Create Course**

_Create a new course container for problems_

```bash
POST http://localhost:9090/Course
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "title": "Data Structures & Algorithms",
  "description": "Complete DSA course with 100+ problems",
  "userName": "testuser",
  "totalQuestions": "100",
  "progress": 0,
  "rating": 5,
  "image": "https://example.com/dsa.jpg",
  "type": "Official",
  "permission": "public",
  "language": ["Java", "Python", "C++"]
}
```

**Expected Response (201 Created):**

```json
{
  "courseId": "65f2b3c4d5e6f7g8h9i0j1k2"
}
```

**💾 Save this `courseId` for next steps!**

---

#### **Step 6: Create Problem Under Course**

_Add a coding problem referenced to the course_

```bash
POST http://localhost:9090/Posts/Course/Data Structures & Algorithms/username/testuser
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "title": "Two Sum",
  "description": "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
  "Example": "Input: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: nums[0] + nums[1] = 2 + 7 = 9",
  "difficulty": "Easy",
  "tags": ["Array", "Hash Table"],
  "companies": ["Amazon", "Google", "Facebook"],
  "accuracy": "45.2%",
  "constrain": "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9",
  "timecomplixity": "O(n)",
  "type": "Coding",
  "solution": {
    "java": {
      "solution": "class Solution {\n  public int[] twoSum(int[] nums, int target) {\n    Map<Integer, Integer> map = new HashMap<>();\n    for (int i = 0; i < nums.length; i++) {\n      int complement = target - nums[i];\n      if (map.containsKey(complement)) {\n        return new int[] { map.get(complement), i };\n      }\n      map.put(nums[i], i);\n    }\n    return new int[] {};\n  }\n}"
    },
    "python": {
      "solution": "def twoSum(nums, target):\n    seen = {}\n    for i, num in enumerate(nums):\n        if target - num in seen:\n            return [seen[target - num], i]\n        seen[num] = i\n    return []"
    }
  },
  "testcase": {
    "test1": "[2,7,11,15], 9",
    "test2": "[3,2,4], 6",
    "test3": "[3,3], 6"
  },
  "codeTemplates": {
    "java": {
      "templateCode": "class Solution {\n  public int[] twoSum(int[] nums, int target) {\n    // Write your code here\n  }\n}",
      "boilerCode": "public class Main {\n  public static void main(String[] args) {\n    Solution s = new Solution();\n    int[] result = s.twoSum(new int[]{2,7,11,15}, 9);\n  }\n}"
    }
  }
}
```

**Expected Response (201 Created):**

```json
{
  "id": "65f3c4d5e6f7g8h9i0j1k2l3",
  "title": "Two Sum",
  "description": "...",
  "difficulty": "Easy",
  "course": {
    "id": "65f2b3c4d5e6f7g8h9i0j1k2",
    "title": "Data Structures & Algorithms"
  }
}
```

---

#### **Step 7: Get All Problems in Course**

_Fetch all problems associated with the course_

```bash
GET http://localhost:9090/Posts/Course/Data Structures & Algorithms/username/testuser
```

**Expected Response (200 OK):**

```json
[
  {
    "id": "65f3c4d5e6f7g8h9i0j1k2l3",
    "title": "Two Sum",
    "difficulty": "Easy",
    "tags": ["Array", "Hash Table"],
    "accuracy": "45.2%"
  }
  // ... more problems
]
```

---

#### **Step 8: Get Course by ID**

_Retrieve complete course information_

```bash
GET http://localhost:9090/Course/id/65f2b3c4d5e6f7g8h9i0j1k2
```

**Expected Response (200 OK):**

```json
{
  "id": "65f2b3c4d5e6f7g8h9i0j1k2",
  "title": "Data Structures & Algorithms",
  "description": "Complete DSA course with 100+ problems",
  "progress": 0,
  "totalQuestions": "100",
  "rating": 5,
  "posts": [{ "id": "65f3c4d5e6f7g8h9i0j1k2l3", "title": "Two Sum" }]
}
```

---

### ✅ **PHASE 4: Contest Management Flow** (4 steps)

#### **Step 9: Create Contest**

_Create a coding contest event_

```bash
POST http://localhost:9090/Contest
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "nameOfContest": "Winter Coding Challenge 2024",
  "nameOfOrganization": "Code For Challenge",
  "date": "2024-03-15T10:00:00Z",
  "description": "Annual coding competition with exciting problems",
  "bannerImage": "https://example.com/banner.jpg",
  "logo": "https://example.com/logo.jpg",
  "type": "Individual",
  "fee": "Free",
  "eligibility": ["Students", "Professionals", "Anyone interested"],
  "rounds": ["Qualification Round", "Semi-Finals", "Grand Finale"],
  "rewards": ["₹50,000 Prize Pool", "Certificates", "Internship Opportunities"],
  "faq": ["What is the duration?", "Can I use any language?"],
  "faqAnswer": ["3 hours", "Yes, Java, Python, C++ supported"],
  "rules": ["No plagiarism", "Internet allowed", "No collaboration"],
  "language": ["Java", "Python", "C++"],
  "timeDuration": "3 hours"
}
```

**Expected Response (201 Created):**

```json
{
  "ContestID": "65f4d5e6f7g8h9i0j1k2l3m4"
}
```

**💾 Save this `ContestID`!**

---

#### **Step 10: Add Problem to Contest**

_Create a contest-specific problem_

```bash
POST http://localhost:9090/Contest/Winter Coding Challenge 2024/username/testuser
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "title": "Maximum Subarray Sum",
  "description": "Find the contiguous subarray with the largest sum.",
  "Example": "Input: [-2,1,-3,4,-1,2,1,-5,4]\nOutput: 6\nExplanation: [4,-1,2,1] has the largest sum = 6",
  "difficulty": "Medium",
  "tags": ["Array", "Dynamic Programming", "Kadane's Algorithm"],
  "type": "Coding",
  "sequence": "1",
  "solution": {
    "java": {
      "solution": "class Solution {\n  public int maxSubArray(int[] nums) {\n    int max = nums[0], current = nums[0];\n    for (int i = 1; i < nums.length; i++) {\n      current = Math.max(nums[i], current + nums[i]);\n      max = Math.max(max, current);\n    }\n    return max;\n  }\n}"
    }
  }
}
```

**Expected Response (201 Created):**

```json
{
  "id": "65f5e6f7g8h9i0j1k2l3m4n5",
  "title": "Maximum Subarray Sum",
  "contest": {
    "id": "65f4d5e6f7g8h9i0j1k2l3m4",
    "nameOfContest": "Winter Coding Challenge 2024"
  }
}
```

---

#### **Step 11: Create User Contest Details (Participation)**

_Track user participation in contest_

```bash
POST http://localhost:9090/UserDetailsContest
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "nameOfContest": "Winter Coding Challenge 2024",
  "nameOfOrganization": "Code For Challenge",
  "date": "2024-03-15T10:00:00Z",
  "type": "Individual",
  "fee": "Free",
  "rounds": ["Qualification Round"],
  "language": ["Java"],
  "timeTaken": "02:30:00",
  "endTime": "2024-03-15T12:30:00Z"
}
```

**Expected Response (201 Created):**

```json
{
  "id": "65f6f7g8h9i0j1k2l3m4n5o6",
  "nameOfContest": "Winter Coding Challenge 2024",
  "timeTaken": "02:30:00",
  "endTime": "2024-03-15T12:30:00Z"
}
```

---

#### **Step 12: Get User Contest Details**

_Retrieve participation records_

```bash
GET http://localhost:9090/UserDetailsContest/testuser/Winter Coding Challenge 2024
```

**Expected Response (200 OK):**

```json
[
  {
    "id": "65f6f7g8h9i0j1k2l3m4n5o6",
    "nameOfContest": "Winter Coding Challenge 2024",
    "timeTaken": "02:30:00",
    "endTime": "2024-03-15T12:30:00Z",
    "posts": []
  }
]
```

---

### ✅ **PHASE 5: Lecture Management Flow** (3 steps)

#### **Step 13: Create Lecture**

_Create educational content with hierarchical structure_

```bash
POST http://localhost:9090/Lecture
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "title": "Introduction to Java Programming",
  "author": "testuser",
  "headings": [
    {
      "title": "Getting Started with Java",
      "subHeadings": [
        {
          "title": "What is Java?",
          "content": "Java is a high-level, class-based, object-oriented programming language..."
        },
        {
          "title": "Installing JDK",
          "content": "To start programming in Java, you need to install the Java Development Kit (JDK)..."
        }
      ]
    },
    {
      "title": "Variables and Data Types",
      "subHeadings": [
        {
          "title": "Primitive Data Types",
          "content": "Java has 8 primitive data types: byte, short, int, long, float, double, boolean, char..."
        },
        {
          "title": "Variable Declaration",
          "content": "Variables in Java must be declared before use:\nint age = 25;\nString name = 'John';"
        }
      ]
    },
    {
      "title": "Control Flow Statements",
      "subHeadings": [
        {
          "title": "If-Else Statements",
          "content": "Control the flow of execution based on conditions..."
        },
        {
          "title": "Loops",
          "content": "Java provides for, while, and do-while loops for iteration..."
        }
      ]
    }
  ]
}
```

**Expected Response (201 Created):**

```json
{
  "LectureID": "65f7g8h9i0j1k2l3m4n5o6p7"
}
```

---

#### **Step 14: Get Lecture by ID**

_Retrieve complete lecture content_

```bash
GET http://localhost:9090/Lecture/id/65f7g8h9i0j1k2l3m4n5o6p7
```

**Expected Response (200 OK):**

```json
{
  "id": "65f7g8h9i0j1k2l3m4n5o6p7",
  "title": "Introduction to Java Programming",
  "author": "testuser",
  "headings": [
    {
      "title": "Getting Started with Java",
      "subHeadings": [...]
    }
  ]
}
```

---

#### **Step 15: Remove Headings from Lecture**

_Delete specific sections from lecture_

```bash
PUT http://localhost:9090/Lecture/removeHeadings/id/65f7g8h9i0j1k2l3m4n5o6p7
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "headingsToRemove": ["Control Flow Statements"],
  "subHeadingsToRemove": ["Installing JDK"]
}
```

**Expected Response (200 OK):**

```json
{
  "id": "65f7g8h9i0j1k2l3m4n5o6p7",
  "title": "Introduction to Java Programming",
  "headings": [
    // Updated headings without removed sections
  ]
}
```

---

### ✅ **PHASE 6: Topic Skills (Learning Tree)** (2 steps)

#### **Step 16: Create Topic Skill**

_Build hierarchical skill tree with problems_

```bash
POST http://localhost:9090/TopicWiseSkills
Authorization: Basic testuser:test123
Content-Type: application/json

{
  "name": "Data Structures",
  "children": [
    {
      "name": "Arrays",
      "children": [],
      "problem": [
        {
          "id": "prob1",
          "title": "Two Sum",
          "description": "Find two numbers that add up to target",
          "difficulty": "Easy",
          "solution": {
            "solutions": {
              "java": {
                "solution": "Map-based solution..."
              }
            }
          },
          "codeTemplates": {
            "java": {
              "templateCode": "class Solution { ... }",
              "boilerCode": "public class Main { ... }"
            }
          }
        }
      ]
    },
    {
      "name": "Linked Lists",
      "children": [],
      "problem": [
        {
          "id": "prob2",
          "title": "Reverse Linked List",
          "description": "Reverse a singly linked list",
          "difficulty": "Easy"
        }
      ]
    }
  ]
}
```

**Expected Response (201 Created):**

```json
{
  "LectureID": "65f8h9i0j1k2l3m4n5o6p7q8"
}
```

---

#### **Step 17: Get Topic Skill by ID**

_Retrieve complete skill tree_

```bash
GET http://localhost:9090/TopicWiseSkills/id/65f8h9i0j1k2l3m4n5o6p7q8
```

**Expected Response (200 OK):**

```json
{
  "id": "65f8h9i0j1k2l3m4n5o6p7q8",
  "name": "Data Structures",
  "children": [
    {
      "name": "Arrays",
      "problem": [...]
    }
  ]
}
```

---

### ✅ **PHASE 7: File Upload** (1 step)

#### **Step 18: Upload File (Image/Document)**

_Upload media files and get URL_

**In Postman:**

1. Method: `POST`
2. URL: `http://localhost:9090/Files/upload`
3. Body → `form-data`
4. Key: `file` (Type: File)
5. Value: Select your image file

**Expected Response (200 OK):**

```json
{
  "location": "http://localhost:9090/images/profile.jpg"
}
```

**💡 Use this URL in user `profileImg`, course `image`, etc.**

---

### ✅ **PHASE 8: Advanced Operations** (Optional)

#### **Update Course**

```bash
PUT http://localhost:9090/Course/id/65f2b3c4d5e6f7g8h9i0j1k2
Authorization: Basic testuser:test123
{
  "title": "Advanced DSA",
  "progress": 50,
  "totalQuestions": "150"
}
```

#### **Delete Problem**

```bash
DELETE http://localhost:9090/Posts/id/65f3c4d5e6f7g8h9i0j1k2l3
Authorization: Basic testuser:test123
```

#### **Filter Problems by Tags**

```bash
GET http://localhost:9090/Posts/filter?tags=Array&tags=Hash%20Table&exactMatch=true
Authorization: Basic testuser:test123
```

#### **Get All Users (Paginated)**

```bash
GET http://localhost:9090/Public/getAllUsers?page=0&size=10
```

---

## 🎓 Mental Model Summary

### **Entity Relationships:**

```
User (Parent Entity)
│
├── courses[] ──────┐
│                   │
├── contests[] ─────┼──┐
│                   │  │
├── lectures[]      │  │
│                   │  │
├── posts[] ←───────┘  │
│         ↑            │
│         │            │
│         └────────────┘
│
├── topicSkill[]
│
└── userContestDetails[]

Course/Contest (Container)
│
└── posts[] (References back to User.posts)

Lecture (Standalone)
│
└── headings[]
    └── subHeadings[]

TopicSkill (Tree Structure)
│
├── children[] (recursive)
│
└── problem[]
```

### **Key Concepts:**

1. **User is Central** - Everything belongs to a user
2. **DBRef Relationships** - Courses/Contests reference Posts bidirectionally
3. **Hierarchical Structures** - Lectures and TopicSkills have nested data
4. **Authentication Layer** - Protected routes need Basic Auth
5. **Public Access** - `/Public/*` endpoints don't need auth

---

## 🧪 Complete Test Collection Order

**Recommended Postman Collection Order:**

```
📁 1. Setup
  └── Health Check

📁 2. User Management
  ├── Create User (Signup)
  ├── Login
  └── Get Current User

📁 3. Course Flow
  ├── Create Course
  ├── Add Problem to Course
  ├── Get Course Problems
  └── Get Course by ID

📁 4. Contest Flow
  ├── Create Contest
  ├── Add Problem to Contest
  ├── Create User Contest Details
  └── Get User Contest Details

📁 5. Lectures
  ├── Create Lecture
  ├── Get Lecture by ID
  └── Remove Headings

📁 6. Skills
  ├── Create Topic Skill
  └── Get Topic Skill

📁 7. File Operations
  └── Upload File

📁 8. Advanced
  ├── Update Entities
  ├── Delete Entities
  └── Filter/Search
```

---

## 🔐 Authentication & User Management

| Method   | Endpoint                             | Auth | Purpose                        | Request Body                                                               |
| -------- | ------------------------------------ | ---- | ------------------------------ | -------------------------------------------------------------------------- |
| `POST`   | `/Public/Create-User`                | ❌   | **Create new user (Signup)**   | `{"name":"user","email":"user@ex.com","password":"pass","roles":["USER"]}` |
| `POST`   | `/users/login`                       | ❌   | **Login user**                 | `{"name":"user","password":"pass"}`                                        |
| `GET`    | `/users/getUser`                     | ✅   | Get current authenticated user | -                                                                          |
| `PUT`    | `/users`                             | ✅   | Update user profile            | `{"name":"updated","email":"new@email.com","collage":"MIT",...}`           |
| `GET`    | `/Public/showUser/{username}`        | ❌   | Get user by username           | -                                                                          |
| `GET`    | `/Public/{id}`                       | ❌   | Get user by ID                 | -                                                                          |
| `GET`    | `/Public/getAllUsers?page=0&size=10` | ❌   | Get all users (paginated)      | -                                                                          |
| `DELETE` | `/Public/user/id/{id}`               | ❌   | Delete user by ID              | -                                                                          |
| `GET`    | `/Public/HealthCheck`                | ❌   | Server health status           | -                                                                          |

**Sample Request - Create User:**

```json
{
  "name": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "roles": ["USER"],
  "profileImg": "https://example.com/img.jpg"
}
```

---

## 📚 Course Management

| Method   | Endpoint                                      | Auth | Purpose                          | Request Body                                                              |
| -------- | --------------------------------------------- | ---- | -------------------------------- | ------------------------------------------------------------------------- |
| `GET`    | `/Course?page=0&size=10`                      | ❌   | Get all courses (paginated)      | -                                                                         |
| `GET`    | `/Course/user?userName={user}&page=0&size=10` | ❌   | Get courses by username          | -                                                                         |
| `GET`    | `/Course/{username}/{skip}/{limit}`           | ❌   | Get user courses with pagination | -                                                                         |
| `GET`    | `/Course/id/{id}`                             | ❌   | Get course by ID                 | -                                                                         |
| `POST`   | `/Course`                                     | ✅   | Create new course                | `{"title":"Java Course","description":"Learn Java","type":"programming"}` |
| `PUT`    | `/Course/id/{id}`                             | ✅   | Update course by ID              | `{"title":"Updated","description":"New desc"}`                            |
| `DELETE` | `/Course/id/{id}`                             | ✅   | Delete course by ID              | -                                                                         |

**Sample Request - Create Course:**

```json
{
  "title": "Data Structures",
  "description": "Complete DSA course",
  "userName": "admin",
  "totalQuestions": "100",
  "rating": 5,
  "image": "https://example.com/dsa.jpg",
  "type": "Official",
  "permission": "public",
  "language": ["Java", "Python"]
}
```

**Sample Response:**

```json
{
  "courseId": "65a1b2c3d4e5f6g7h8i9j0k1"
}
```

---

## 🏆 Contest Management

| Method   | Endpoint                                     | Auth | Purpose                  | Request Body                                                      |
| -------- | -------------------------------------------- | ---- | ------------------------ | ----------------------------------------------------------------- |
| `GET`    | `/Contest`                                   | ❌   | Get all contests         | -                                                                 |
| `GET`    | `/Contest/{username}`                        | ❌   | Get contests by username | -                                                                 |
| `GET`    | `/Contest/id/{id}`                           | ❌   | Get contest by ID        | -                                                                 |
| `POST`   | `/Contest`                                   | ✅   | Create new contest       | `{"nameOfContest":"CodeJam","date":"2024-01-01","type":"coding"}` |
| `POST`   | `/Contest/{contestName}/username/{username}` | ✅   | Create post in contest   | Post object                                                       |
| `PUT`    | `/Contest/id/{id}`                           | ✅   | Update contest by ID     | Contest object                                                    |
| `DELETE` | `/Contest/id/{id}`                           | ✅   | Delete contest by ID     | -                                                                 |

**Sample Request - Create Contest:**

```json
{
  "nameOfContest": "Winter Coding Challenge",
  "nameOfOrganization": "CFC",
  "date": "2024-02-15T00:00:00Z",
  "description": "Annual coding competition",
  "bannerImage": "https://example.com/banner.jpg",
  "logo": "https://example.com/logo.jpg",
  "type": "Individual",
  "fee": "Free",
  "eligibility": ["Students", "Professionals"],
  "rounds": ["Round 1", "Round 2", "Final"],
  "rewards": ["₹10,000", "Certificate"],
  "language": ["Java", "Python"],
  "timeDuration": "3 hours"
}
```

---

## 📝 Post/Problem Management

| Method   | Endpoint                                           | Auth | Purpose                           | Request Body |
| -------- | -------------------------------------------------- | ---- | --------------------------------- | ------------ |
| `GET`    | `/Posts/ProblemSet`                                | ❌   | Get all posts/problems            | -            |
| `GET`    | `/Posts/username/{username}`                       | ❌   | Get posts by username             | -            |
| `GET`    | `/Posts/username/{username}/posts?page=0&size=10`  | ❌   | Get posts paginated               | -            |
| `GET`    | `/Posts/id/{id}`                                   | ❌   | Get post by ID                    | -            |
| `GET`    | `/Posts/Course/{courseName}/username/{username}`   | ❌   | Get posts by course               | -            |
| `GET`    | `/Posts/Contest/{contestName}/username/{username}` | ❌   | Get posts by contest              | -            |
| `GET`    | `/Posts/filter?tags={tags}&exactMatch=true`        | ✅   | Filter posts by tags              | -            |
| `POST`   | `/Posts/username/{username}`                       | ✅   | Create new post                   | Post object  |
| `POST`   | `/Posts/Course/{courseName}/username/{username}`   | ✅   | Create post with course reference | Post object  |
| `PUT`    | `/Posts/username/{username}/id/{id}`               | ✅   | Update post by ID                 | Post object  |
| `DELETE` | `/Posts/id/{id}`                                   | ✅   | Delete post by ID                 | -            |

**Sample Request - Create Post/Problem:**

```json
{
  "title": "Two Sum Problem",
  "description": "Find two numbers that add up to target",
  "Example": "Input: [2,7,11,15], target=9\nOutput: [0,1]",
  "difficulty": "Easy",
  "solution": {
    "java": { "solution": "class Solution { ... }" },
    "python": { "solution": "def twoSum(): ..." }
  },
  "tags": ["Array", "Hash Table"],
  "companies": ["Amazon", "Google"],
  "accuracy": "45%",
  "constrain": "1 <= nums.length <= 10^4",
  "timecomplixity": "O(n)",
  "type": "Coding",
  "testcase": {
    "test1": "[2,7,11,15], 9",
    "test2": "[3,2,4], 6"
  },
  "codeTemplates": {
    "java": {
      "templateCode": "class Solution {\n  public int[] twoSum(int[] nums, int target) {\n    // Your code here\n  }\n}",
      "boilerCode": "public class Main { ... }"
    }
  }
}
```

---

## 📖 Lecture Management

| Method   | Endpoint                                    | Auth | Purpose                       | Request Body                                       |
| -------- | ------------------------------------------- | ---- | ----------------------------- | -------------------------------------------------- |
| `GET`    | `/Lecture`                                  | ❌   | Get all lectures              | -                                                  |
| `GET`    | `/Lecture/{username}`                       | ❌   | Get lectures by username      | -                                                  |
| `GET`    | `/Lecture/id/{id}`                          | ❌   | Get lecture by ID             | -                                                  |
| `GET`    | `/Lecture/Findby/{username}/{lectureTitle}` | ❌   | Get lecture by user and title | -                                                  |
| `POST`   | `/Lecture`                                  | ✅   | Create new lecture            | Lecture object                                     |
| `PUT`    | `/Lecture/id/{id}`                          | ✅   | Update lecture by ID          | Lecture object                                     |
| `PUT`    | `/Lecture/removeHeadings/id/{id}`           | ✅   | Remove headings/subheadings   | `{"headingsToRemove":[],"subHeadingsToRemove":[]}` |
| `DELETE` | `/Lecture/id/{id}`                          | ✅   | Delete lecture by ID          | -                                                  |

**Sample Request - Create Lecture:**

```json
{
  "title": "Introduction to Java",
  "author": "admin",
  "headings": [
    {
      "title": "Getting Started",
      "subHeadings": [
        {
          "title": "Installation",
          "content": "To install Java, download JDK..."
        },
        {
          "title": "Hello World",
          "content": "Your first Java program..."
        }
      ]
    },
    {
      "title": "Variables",
      "subHeadings": [
        {
          "title": "Data Types",
          "content": "Java has primitive types..."
        }
      ]
    }
  ]
}
```

---

## 👤 User Contest Details

| Method   | Endpoint                                                | Auth | Purpose                          | Request Body              |
| -------- | ------------------------------------------------------- | ---- | -------------------------------- | ------------------------- |
| `GET`    | `/UserDetailsContest`                                   | ❌   | Get all user contest details     | -                         |
| `GET`    | `/UserDetailsContest/{username}/{contestName}`          | ❌   | Get user contest details         | -                         |
| `GET`    | `/UserDetailsContest/findby/{contestName}`              | ❌   | Get users by contest name        | -                         |
| `GET`    | `/UserDetailsContest/id/{id}`                           | ❌   | Get details by ID                | -                         |
| `POST`   | `/UserDetailsContest`                                   | ✅   | Create user contest details      | UserContestDetails object |
| `POST`   | `/UserDetailsContest/{contestName}/username/{username}` | ✅   | Create post with contest details | Post object               |
| `PUT`    | `/UserDetailsContest/id/{id}`                           | ✅   | Update contest details           | UserContestDetails object |
| `DELETE` | `/UserDetailsContest/id/{id}`                           | ✅   | Delete contest details           | -                         |

**Sample Request - User Contest Details:**

```json
{
  "nameOfContest": "Winter Challenge",
  "nameOfOrganization": "CFC",
  "date": "2024-02-15T00:00:00Z",
  "type": "Individual",
  "team": [],
  "fee": "Free",
  "rounds": ["Qualification", "Final"],
  "rewards": ["Certificate"],
  "language": ["Java"],
  "timeTaken": "2:30:00",
  "endTime": "2024-02-15T15:30:00Z"
}
```

---

## 🎯 Topic Skills

| Method   | Endpoint                      | Auth | Purpose                      | Request Body      |
| -------- | ----------------------------- | ---- | ---------------------------- | ----------------- |
| `GET`    | `/TopicWiseSkills`            | ❌   | Get all topic skills         | -                 |
| `GET`    | `/TopicWiseSkills/{username}` | ❌   | Get topic skills by username | -                 |
| `GET`    | `/TopicWiseSkills/id/{id}`    | ❌   | Get topic skill by ID        | -                 |
| `POST`   | `/TopicWiseSkills`            | ✅   | Create topic skill           | TopicSkill object |
| `PUT`    | `/TopicWiseSkills/id/{id}`    | ✅   | Update topic skill           | TopicSkill object |
| `DELETE` | `/TopicWiseSkills/id/{id}`    | ✅   | Delete topic skill           | -                 |

**Sample Request - Create Topic Skill:**

```json
{
  "name": "Data Structures",
  "children": [
    {
      "name": "Arrays",
      "problem": [
        {
          "title": "Two Sum",
          "description": "Find two numbers...",
          "difficulty": "Easy",
          "solution": {
            "solutions": {
              "java": { "solution": "..." }
            }
          }
        }
      ]
    }
  ]
}
```

---

## 📁 File Upload

| Method | Endpoint        | Auth | Purpose                      | Request Body                            |
| ------ | --------------- | ---- | ---------------------------- | --------------------------------------- |
| `POST` | `/Files/upload` | ❌   | Upload file (image/document) | `multipart/form-data` with `file` field |

**Sample Request - File Upload (Postman):**

1. Method: `POST`
2. URL: `http://localhost:9090/Files/upload`
3. Body: Select `form-data`
4. Key: `file` (type: File)
5. Value: Choose file to upload

**Sample Response:**

```json
{
  "location": "http://localhost:9090/images/myfile.jpg"
}
```

---

## 🔑 Authentication Notes

- **✅ Protected Routes:** Require HTTP Basic Authentication
  - Username: Your registered username
  - Password: Your password
  - **In Postman:** Authorization → Basic Auth → Enter credentials

- **❌ Public Routes:** No authentication required

---

## 📋 Common Response Codes

| Code                        | Meaning                                |
| --------------------------- | -------------------------------------- |
| `200 OK`                    | Request successful                     |
| `201 CREATED`               | Resource created successfully          |
| `204 NO CONTENT`            | Update successful, no content returned |
| `400 BAD REQUEST`           | Invalid request data                   |
| `401 UNAUTHORIZED`          | Authentication required or failed      |
| `404 NOT FOUND`             | Resource not found                     |
| `409 CONFLICT`              | Resource already exists (duplicate)    |
| `500 INTERNAL SERVER ERROR` | Server error                           |

---

## 🎯 Quick Testing Guide

### 1. Create Admin User

```bash
POST http://localhost:9090/Public/Create-User
{
  "name": "admin",
  "email": "admin@cfc.com",
  "password": "admin123",
  "roles": ["ADMIN", "USER"]
}
```

### 2. Login

```bash
POST http://localhost:9090/users/login
{
  "name": "admin",
  "password": "admin123"
}
```

### 3. Create Course (with Basic Auth)

```bash
POST http://localhost:9090/Course
Authorization: Basic admin:admin123
{
  "title": "DSA Course",
  "description": "Complete Data Structures"
}
```

### 4. Create Problem

```bash
POST http://localhost:9090/Posts/username/admin
Authorization: Basic admin:admin123
{
  "title": "Two Sum",
  "description": "Array problem",
  "difficulty": "Easy",
  "tags": ["Array", "Hash Table"]
}
```

---

## 💡 Tips

1. **Always encode passwords** - Passwords are auto-encrypted with BCrypt
2. **Use pagination** - Most list endpoints support `?page=0&size=10`
3. **Check authentication** - Protected routes need Basic Auth headers
4. **Unique usernames** - Sign up returns 409 if username exists
5. **CORS enabled** - Frontend at localhost:5173 is whitelisted

---

## 🐛 Need Help?

- Check if server is running on port 9090
- Verify MongoDB is connected
- Review application logs for errors
- Ensure correct authentication for protected routes

---

**Last Updated:** 2026-02-03
