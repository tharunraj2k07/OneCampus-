# OneCampus AI — Backend API Engine

The core backend service for **OneCampus AI**, an AI-powered personalized college communication and student productivity platform. Built with **Node.js**, **Express.js**, **TypeScript**, and **MongoDB** using clean architecture principles.

---

## 🛠️ Technology Stack

- **Runtime**: Node.js (v18+)
- **Language**: TypeScript (v5+)
- **Framework**: Express.js
- **Database & ODM**: MongoDB with Mongoose (v8+)
- **Environment & Utilities**: dotenv, cors

---

## 📁 Architecture & Directory Structure

```
backend/
├── .env.example                # Environment variables template
├── README.md                   # Backend documentation & setup guide
├── package.json                # Project dependencies & npm scripts
├── tsconfig.json               # TypeScript compiler configuration
└── src/
    ├── app.ts                  # Express application setup & middleware chain
    ├── server.ts               # Server entry point & graceful shutdown
    ├── config/
    │   ├── db.ts               # MongoDB connection lifecycle & listeners
    │   └── env.ts              # Type-safe environment variable parsing
    ├── constants/
    │   └── index.ts            # System constants & error codes
    ├── controllers/
    │   ├── announcement.controller.ts # Announcement request orchestration
    │   ├── health.controller.ts       # Health check controller
    │   └── index.ts
    ├── middleware/
    │   ├── asyncHandler.ts     # Async controller wrapper for unhandled promises
    │   ├── errorHandler.ts     # Global standard error interceptor
    │   ├── notFoundHandler.ts  # 404 Route Not Found handler
    │   └── index.ts
    ├── models/
    │   ├── Announcement.ts     # Circular & announcement schema with AI placeholders
    │   ├── Bookmark.ts         # User bookmarked announcements
    │   ├── FacultyProfile.ts   # Faculty department & designation schema
    │   ├── Notification.ts     # Targeted campus alerts & push notifications
    │   ├── StudentProfile.ts   # Student academic metadata & interests
    │   ├── Task.ts             # Student academic deadline & checklist task schema
    │   ├── User.ts             # Core authentication & user profile schema
    │   └── index.ts
    ├── repositories/
    │   ├── base.repository.ts  # Generic type-safe MongoDB repository
    │   ├── announcement.repository.ts # Filtered pagination & search queries
    │   └── index.ts
    ├── routes/
    │   ├── index.ts            # Root API router (/api/v1 and /api/health)
    │   └── v1/
    │       ├── announcement.routes.ts # /api/v1/announcements
    │       ├── health.routes.ts       # /api/v1/health
    │       └── index.ts
    ├── services/
    │   ├── announcement.service.ts    # Announcement business logic
    │   ├── health.service.ts          # System telemetry & DB status service
    │   └── index.ts
    ├── types/
    │   ├── api.types.ts        # Unified API success & error response interfaces
    │   ├── announcement.types.ts # Announcement enums and types
    │   ├── bookmark.types.ts   # Bookmark interfaces
    │   ├── notification.types.ts # Notification categories and types
    │   ├── profile.types.ts    # Student and Faculty profile types
    │   ├── task.types.ts       # Task status and types
    │   ├── user.types.ts       # User roles and interfaces
    │   └── index.ts
    ├── utils/
    │   ├── apiError.ts         # Centralized HTTP error representation
    │   ├── apiResponse.ts      # Standardized JSON response formatting
    │   └── logger.ts           # Standard application logger
    └── validators/
        ├── announcement.validator.ts # Input validation for announcements
        └── index.ts
```

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | Server listening port | `5000` |
| `NODE_ENV` | Environment mode (`development`, `production`, `test`) | `development` |
| `MONGODB_URI` | MongoDB connection URI | `mongodb://localhost:27017/onecampus_ai` |
| `CORS_ORIGIN` | Allowed CORS origins (comma-separated or `*`) | `http://localhost:3000,http://10.0.2.2:5000` |
| `JWT_SECRET` | Secret key for token signing (Phase 5) | *(Placeholder)* |
| `GEMINI_API_KEY` | Google Gemini API Key for circular parsing (Phase 7) | *(Placeholder)* |
| `FCM_CONFIGURATION` | Firebase Cloud Messaging configuration (Phase 8) | *(Placeholder)* |

---

## 🚀 Getting Started

### 1. Install Dependencies
```bash
cd backend
npm install
```

### 2. Type Checking & Compilation
```bash
npm run type-check
npm run build
```

### 3. Running the Server

- **Development Mode (ts-node)**:
  ```bash
  npm run dev
  ```

- **Production Mode (Compiled JS)**:
  ```bash
  npm run build
  npm start
  ```

---

## 📡 API Endpoints

### Standard Response Format

#### Success
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

#### Error
```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "details": {}
  }
}
```

---

### Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/health` | System health check, environment, and DB status |
| `GET` | `/api/health` | Health check alias |
| `GET` | `/api/v1/announcements` | List announcements (supports category, dept, year filters & pagination) |
| `GET` | `/api/v1/announcements/:id` | Get single announcement by ID |
| `POST` | `/api/v1/announcements` | Create new circular/announcement |
| `PUT` | `/api/v1/announcements/:id` | Update existing announcement |
| `DELETE` | `/api/v1/announcements/:id` | Delete announcement |

#### Sample Health Endpoint Response (`GET /api/v1/health`)
```json
{
  "success": true,
  "message": "OneCampus AI API is running",
  "data": {
    "status": "healthy",
    "environment": "development",
    "timestamp": "2026-08-29T17:00:00.000Z",
    "uptimeSeconds": 42,
    "database": {
      "connected": true,
      "status": "CONNECTED"
    },
    "version": "1.0.0"
  }
}
```
