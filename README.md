# OneCampus AI — Complete System Documentation

OneCampus AI is an enterprise-grade, AI-augmented campus communication and student productivity platform. It eliminates notice fragmentation, infers deadlines, automates task generation, delivers personalized feeds, and supports offline-first synchronization.

---

## 🏛️ System Architecture

```
                                    ┌────────────────────────────┐
                                    │   Gemini 1.5/2.0 Flash     │
                                    │   Information Extraction   │
                                    └─────────────▲──────────────┘
                                                  │ (REST API)
                                    ┌─────────────┴──────────────┐
                                    │   OneCampus AI Backend     │
                                    │   (Node.js + Express + TS) │
                                    └──────┬──────────────┬──────┘
                                           │              │
                   ┌───────────────────────┴──┐        ┌──┴─────────────────────────┐
                   │  MongoDB Atlas / Replica │        │  Firebase Cloud Messaging  │
                   │  Indexed Document Store  │        │  Push Notification Service │
                   └──────────────────────────┘        └────────────────────────────┘
                                           ▲
                                           │ (Retrofit HTTP / JSON REST)
                                           ▼
                    ┌────────────────────────────────────────────────────────┐
                    │               OneCampus AI Android App                 │
                    │               (Kotlin + Jetpack Compose)               │
                    │                                                        │
                    │  ┌─────────────────────────┐ ┌──────────────────────┐  │
                    │  │ Room Local SQLite Cache │ │ WorkManager Periodic │  │
                    │  │ (Announcements / Tasks) │ │ Offline Sync Queue   │  │
                    │  └─────────────────────────┘ └──────────────────────┘  │
                    │  ┌─────────────────────────┐ ┌──────────────────────┐  │
                    │  │ Presentation Layer: M3  │ │ Live Demo Repository │  │
                    │  │ Responsive StateFlow    │ │ (Zero-Key Safe Mode) │  │
                    │  └─────────────────────────┘ └──────────────────────┘  │
                    └────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Layer | Technologies & Frameworks |
| :--- | :--- |
| **Android Client** | Kotlin, Jetpack Compose, Material Design 3, Coroutines, Flow, StateFlow |
| **Local Persistence** | Android Room Database (SQLite), Shared Preferences, Encrypted Data |
| **Background Sync** | Android WorkManager, ConnectivityManager Network Callbacks |
| **Push Notifications** | Firebase Cloud Messaging (FCM) v1 HTTP API, Android O+ Notification Channels |
| **Backend API Engine** | Node.js (v18+), Express.js, TypeScript (v5), Mongoose ODM |
| **AI Processing** | Google Gemini Generative Language API (`gemini-1.5-flash` / `gemini-2.5-flash`) |
| **Database** | MongoDB with Compound Indexes for Department, Year, Deadline, Priority |
| **Security** | JWT (RS256/HS256), bcryptjs password hashing, Role-Based Access Control (RBAC) |

---

## ⚙️ Environment Configuration

### Backend (`/backend/.env`)

```env
# Server Configuration
PORT=5000
NODE_ENV=development
MONGODB_URI=mongodb://localhost:27017/onecampus_ai
CORS_ORIGIN=*

# Security
JWT_SECRET=your_production_grade_jwt_secret_minimum_32_characters
JWT_EXPIRES_IN=7d

# Gemini AI Engine
GEMINI_API_KEY=your_gemini_api_key_here

# Firebase Cloud Messaging
FCM_CONFIGURATION={"type":"service_account","project_id":"onecampus-ai","private_key":"..."}
```

### Android Client (`app/build.gradle.kts` / `local.properties` / AI Studio Secrets)

- `BASE_URL`: Defaults to `http://10.0.2.2:5000/api/v1/` for local Android Emulator or Cloud Run URL.
- Demo mode is built directly into `DemoRepository.kt` so the app functions seamlessly for live presentations even without active internet or backend setup.

---

## 🚀 Setup & Execution Guide

### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Compile TypeScript
npm run build

# Start server in production mode
npm start

# Or start in development watch mode
npm run dev
```

### 2. Android App Compilation & Testing

```bash
# Compile and build debug APK
gradle :app:assembleDebug

# Run Robolectric and JVM unit tests
gradle :app:testDebugUnitTest
```

---

## 🔄 Core Verification Workflows

### 1. Faculty Announcement Publishing
1. Faculty authenticates with verified credentials (`Role.FACULTY`).
2. Drafts announcement with title, content, target departments, target years, and optional deadline.
3. Clicks **Publish**:
   - Backend saves announcement with status `PUBLISHED`.
   - Triggers asynchronous Gemini extraction:
     - Extracts concise summary, actionable tasks, keywords, and eligibility.
     - Computes deterministic hybrid priority score (0–100) and priority level (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
     - Reconciles faculty deadline with AI extracted deadline.
   - Triggers automatic student task synchronization for eligible students.
   - Dispatches targeted push notifications respecting student preferences.

### 2. Student Personalization & Task Completion
1. Student authenticates with student profile (`department: "CSE"`, `year: 3`).
2. Personalized feed ranks relevant notices at the top:
   - Evaluates department match, year match, section match, and interest tags.
   - Mandatory campus notices remain visible to all students regardless of filters.
3. Automatically creates actionable tasks in student's dashboard.
4. Student marks task as completed:
   - Updates local Room database immediately.
   - In offline mode, enqueues update in `OfflineSyncWorker`.
   - On network reconnection, synchronizes with backend using Last-Write-Wins (LWW) conflict resolution.

---

## 🛡️ Security Audit & Role-Based Access Control

- **Password Protection**: Passwords are encrypted using `bcryptjs` with salt rounds = 10. `passwordHash` is excluded from all default queries (`select: false`) and stripped via Mongoose schema `toJSON` transforms.
- **JWT Protection**: Tokens contain standard claims (`userId`, `email`, `role`). Expiration and signature tampering are strictly handled.
- **RBAC Boundaries**:
  - `STUDENT`: Read-only access to published announcements matching their audience. Can modify only their own tasks. Cannot access drafts, edit announcements, or manage other students' tasks.
  - `FACULTY`: Can create announcements, manage their own drafts, publish, edit, and archive their own circulars. Cannot edit circulars owned by other faculty members.
  - `ADMIN`: College-wide administrative control.

---

## 🎯 Project Demonstration Mode (Safe for Presentations)

The mobile application includes a built-in **Demo Mode**:
- Allows immediate switching between **Student Persona** (Rahul Sharma, CSE 3rd Year) and **Faculty Persona** (Dr. Aris Thorne, HOD CSE).
- Includes realistic pre-loaded announcements:
  - Zoho Campus Recruitment Drive (Placement, Critical Priority, Actions: Register, Upload Resume)
  - Smart India Hackathon internal screening (Competition, High Priority)
  - Semester Exam Schedule (Academic, Critical Priority)
- Works completely offline without requiring live backend or API keys.

---

## 📋 Known Limitations & Recommended Future Work

1. **OCR Support**: Current AI extraction processes text and structured metadata. Future versions could integrate Google ML Kit on-device or Cloud Vision for physical poster scanning.
2. **Calendar Sync**: Tasks currently populate the in-app calendar. Android System Calendar Provider sync (`CalendarContract.Events`) can be added in subsequent releases.
3. **Multi-Tenant Campus Partitioning**: Current architecture handles a single university campus; future versions can add multi-institution schema partitioning.
