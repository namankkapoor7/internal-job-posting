# Internal Job Posting (IJP) Platform — Comprehensive Technical Documentation

---

## Table of Contents

1. [Project Introduction & Purpose](#1-project-introduction--purpose)
2. [System Architecture Overview](#2-system-architecture-overview)
3. [Technology Stack](#3-technology-stack)
4. [Microservices — Detailed Breakdown](#4-microservices--detailed-breakdown)
5. [Security — Spring Security, BCrypt & JWT In-Depth](#5-security--spring-security-bcrypt--jwt-in-depth)
6. [Resume / Document Upload Pipeline — In-Depth](#6-resume--document-upload-pipeline--in-depth)
7. [Application Lifecycle State Machine](#7-application-lifecycle-state-machine)
8. [Inter-Service Communication — OpenFeign](#8-inter-service-communication--openfeign)
9. [Database Design](#9-database-design)
10. [Validation Framework — End-to-End](#10-validation-framework--end-to-end)
11. [Notification System](#11-notification-system)
12. [Angular Frontend — Architecture & Key Features](#12-angular-frontend--architecture--key-features)
13. [Unit Testing Strategy](#13-unit-testing-strategy)
14. [Add-On: Security Best Practices Implemented](#14-add-on-security-best-practices-implemented)
15. [API Endpoint Reference](#15-api-endpoint-reference)
16. [How to Start the Project](#16-how-to-start-the-project)
17. [Viva Preparation — Core Concepts Q&A](#17-viva-preparation--core-concepts-qa)

---

## 1. Project Introduction & Purpose

The **Internal Job Posting (IJP) Platform** is a full-stack enterprise web application that allows employees within a company to apply for internal job openings managed by the HR Administration team.

### Problem Statement
In most large organizations, internal job postings are communicated through email chains or notice boards, which are informal, untracked, and hard to audit. The IJP platform digitizes and automates the internal recruitment process.

### Key Stakeholders
| Actor | Role in System |
|---|---|
| **Employee (Candidate)** | Registers, browses jobs, submits applications with resumes |
| **HR Admin** | Reviews applications, shortlists candidates, schedules interviews |
| **System** | Enforces validation, sends in-app notifications, tracks application states |

### Core Features
- Employee registration and secure login (JWT-based)
- Job browsing and searching with filters
- Job application with optional resume upload
- Application lifecycle tracking (7 states)
- HR review panel with stage updates and interview scheduling (Online/Offline)
- In-app notification system for candidates
- Resume/document management with secure download
- Designation Master managed by Admin
- Input validation across both frontend (Angular) and backend (Spring Boot)

---

## 2. System Architecture Overview

The application follows a **Microservices Architecture** where multiple independent services communicate over HTTP. All requests from the Angular frontend pass through a single entry point: the **API Gateway**.

```
Browser (Angular)
        |
   localhost:4200
        |
        ▼
  [API Gateway] <--> [Eureka Service Registry]
   localhost:8080          localhost:8761
        |
  +-----+-----+
  v     v     v
[Candidate] [Job] [Admin]
 Service   Service Service
 :8082     :8081   :8083
  |          |       |
 MySQL      MySQL   MySQL
candidate_db job_db  admin_db
```

### Key Architectural Decisions

| Decision | Reason |
|---|---|
| **Microservices** | Each business domain is independently deployable and scalable |
| **API Gateway** | Single routing point; handles CORS globally |
| **Eureka Service Discovery** | Dynamic service registration; no hardcoded ports in gateway |
| **OpenFeign** | Declarative HTTP client for inter-service calls |
| **JWT Tokens** | Stateless authentication across all services |
| **MySQL with JPA** | Relational data with LONGBLOB for binary file storage |

---

## 3. Technology Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Backend Framework** | Spring Boot | 3.x | Core microservice framework |
| **Service Discovery** | Spring Cloud Netflix Eureka | 2023.x | Service registry |
| **API Gateway** | Spring Cloud Gateway | 2023.x | Routing, CORS, load balancing |
| **Inter-Service HTTP** | OpenFeign | 4.x | Declarative HTTP client |
| **Database** | MySQL | 8.x | Persistent data storage |
| **ORM** | Spring Data JPA / Hibernate | 6.x | Object-relational mapping |
| **Authentication** | JSON Web Tokens (JWT) | JJWT 0.11.x | Stateless auth tokens |
| **Password Security** | BCrypt | Spring Security | Password hashing |
| **File Upload** | Spring Multipart / MultipartFile | Spring Boot | Resume upload |
| **Frontend** | Angular | 17 | Single Page Application |
| **Testing** | JUnit 5 + Mockito | 5.x | Unit testing |
| **Build Tool** | Maven | 3.x | Build and dependency management |

---

## 4. Microservices — Detailed Breakdown

### 4.1 Service Discovery (Eureka)

**Port**: `8761`

```properties
spring.application.name=service-discovery
server.port=8761
eureka.client.register-with-eureka=false   # Does not register itself
eureka.client.fetch-registry=false         # Does not fetch registry from itself
```

**Concept**: Eureka is a **Service Registry**. When any microservice starts, it sends a registration heartbeat to Eureka with its IP address and port. The API Gateway asks Eureka: "Where is CANDIDATE-SERVICE running?" and routes requests accordingly. This enables **dynamic service discovery** — if a service moves to a different port, no configuration changes are needed in the gateway.

---

### 4.2 API Gateway

**Port**: `8080`

The gateway acts as the **single entry point** for the Angular frontend. It performs:
1. **Route matching** based on URL path pattern
2. **Load-balanced forwarding** to the correct microservice
3. **Global CORS policy** configuration

```properties
# Route 1: All /api/jobs/** goes to JOB-SERVICE
spring.cloud.gateway.routes[0].id=job-service
spring.cloud.gateway.routes[0].uri=lb://JOB-SERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/jobs/**

# Route 2: Candidate-related endpoints
spring.cloud.gateway.routes[1].id=candidate-service
spring.cloud.gateway.routes[1].uri=lb://CANDIDATE-SERVICE
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/candidates/**,/api/auth/**,/api/applications/**,/api/hr/**,/api/me/**,/api/documents/**

# Route 3: Admin endpoints
spring.cloud.gateway.routes[2].id=admin-service
spring.cloud.gateway.routes[2].uri=lb://ADMIN-SERVICE
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/admin/**
```

**`lb://` prefix**: Tells the gateway to use Eureka for service discovery and Spring Cloud LoadBalancer to balance traffic across multiple instances.

**CORS**: Configured once centrally rather than in every microservice — allows `localhost:4200` with all HTTP methods and headers.

---

### 4.3 Candidate Service

**Port**: `8082` | **Database**: `candidate_db`

**Package Structure**:
```
candidateservice/
├── CandidateServiceApplication.java  → @EnableDiscoveryClient, @EnableFeignClients
├── security/
│   └── JwtUtil.java                  → Token generation and parsing
├── entity/
│   ├── EmployeeProfile.java          → Employee accounts table
│   ├── Application.java              → Job application records
│   ├── Document.java                 → Resume binary storage
│   ├── Interview.java                → Interview schedule records
│   └── Notification.java             → In-app notifications
├── repository/                        → Spring Data JPA interfaces
├── client/
│   └── JobServiceClient.java         → Feign client to job-service
├── dto/
│   └── JobPostingDto.java            → Data transfer object for job data
├── service/
│   └── CandidateService.java         → All business logic
└── controller/
    └── CandidateController.java      → REST endpoints
```

**Startup Data Initialization** (`@PostConstruct`): When the service starts, `initSampleEmployees()` seeds two test employee accounts (`EMP1001`, `EMP1002`) if the database is empty.

---

### 4.4 Job Service

**Port**: `8081` | **Database**: `job_db`

Handles creating, updating, searching, and closing job postings. Job statuses: `PUBLISHED`, `OPEN`, `DRAFT`, `PAUSED`, `CLOSED`.

**Key validation**:
```java
if (jobPosting.getSalaryMin() != null && jobPosting.getSalaryMin() < 0)
    throw new RuntimeException("Minimum salary cannot be negative!");
if (jobPosting.getSalaryMax() < jobPosting.getSalaryMin())
    throw new RuntimeException("Maximum salary cannot be less than minimum salary!");
```

---

### 4.5 Admin Service

**Port**: `8083` | **Database**: `admin_db`

Handles admin login and designation master management (CRUD). Admin account seeded on startup: `admin@company.com` / `admin123`.

---

## 5. Security — Spring Security, BCrypt & JWT In-Depth

### 5.1 Why JWT for a Microservices System?

Traditional web apps store sessions on the server. In a microservices setup, **server-side sessions would require all services to share session storage** (Redis, etc.), adding complexity.

**JWT (JSON Web Token)** is a stateless alternative:
- The token is cryptographically signed and contains all user information as **claims**
- Any service can independently verify a JWT without contacting a central server
- Tokens expire automatically (24 hours in this project)

### 5.2 BCrypt Password Hashing

```java
// Registration: Hash the plain-text password before storing
profile.setPassword(passwordEncoder.encode(profile.getPassword().trim()));

// Login: Compare plain-text input against the stored hash
boolean isValid = passwordEncoder.matches(inputPassword, storedHash);
```

**How BCrypt works**:
- BCrypt generates a random **salt** for each password
- The salt is embedded in the resulting hash string (e.g., `$2a$10$...`)
- The **cost factor** (default 10) makes the hashing intentionally slow to resist brute-force attacks
- Even if two users have the same password, their stored hashes will be different due to unique salts

**Why you cannot "decrypt" a BCrypt hash**: BCrypt is a one-way function. The only way to verify a password is to run BCrypt on the input with the embedded salt and compare the result — this is what `matches()` does.

---

### 5.3 JwtUtil.java — Token Lifecycle

**JWT Structure** (3 parts separated by `.`):

```
header.payload.signature
```

| Part | Content |
|---|---|
| **Header** | Algorithm: HS256, Type: JWT |
| **Payload (Claims)** | `email`, `role`, `employeeId`, `userId`, `name`, `iat`, `exp` |
| **Signature** | HMAC-SHA256(base64(header) + "." + base64(payload), SECRET_KEY) |

**Token Generation**:
```java
public String generateToken(String email, String role, String employeeId, Long userId, String name) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);          // "EMPLOYEE" or "ADMIN"
    claims.put("employeeId", employeeId);
    claims.put("userId", userId);
    claims.put("name", name);
    claims.put("email", email);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}
```

**Token Validation**: The JJWT library automatically verifies the **signature** when parsing. If someone tampers with the payload, the signature verification fails and an exception is thrown, causing `validateToken()` to return `false`.

---

### 5.4 How Login Works End-to-End

```
Employee fills login form
          |
Angular → POST /api/auth/login { email, password }
          |
API Gateway routes to candidate-service
          |
candidateService.loginEmployee(email, password):
  1. Validate email not null/empty
  2. Check email ends with @company.com
  3. Query DB: findByEmailIgnoreCase(email)
  4. BCrypt verify: passwordEncoder.matches(input, storedHash)
  5. jwtUtil.generateToken(email, "EMPLOYEE", empId, id, name)
  6. Return { token, employeeId, email, firstName, ... }
          |
Angular AuthService:
  localStorage.setItem('ijp_token', token)
  localStorage.setItem('ijp_role', 'EMPLOYEE')
  router.navigate(['/employee-dashboard'])
```

---

### 5.5 How Protected Endpoints Verify JWT

JWT is manually verified per endpoint in the controller:

```java
private String getEmployeeIdFromToken(String authHeader) {
    String token = extractToken(authHeader);  // Strip "Bearer " prefix
    if (token != null && jwtUtil.validateToken(token)) {
        return jwtUtil.extractEmployeeId(token);  // Extract employeeId claim
    }
    return null;  // Invalid or missing token
}

// Usage in every protected endpoint:
@GetMapping("/api/me/documents")
public ResponseEntity<?> getMyDocuments(@RequestHeader(AUTHORIZATION) String authHeader) {
    String empId = getEmployeeIdFromToken(authHeader);
    if (empId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Unauthorized request"));
    }
    // Proceed with business logic
}
```

---

### 5.6 Role-Based Access Control (RBAC)

| Role | Token Claim | Access |
|---|---|---|
| `EMPLOYEE` | `"EMPLOYEE"` | Own profile, own applications, own documents, own notifications |
| `ADMIN` | `"ADMIN"` | All HR operations, all candidates, all documents, designation management |

```java
String role = getRoleFromToken(authHeader);
boolean isHr = "ADMIN".equalsIgnoreCase(role) || "HR_ADMIN".equalsIgnoreCase(role);
```

At the Angular level, **Route Guards** enforce RBAC by checking `localStorage.getItem('ijp_role')`.

---

### 5.7 CORS Configuration

**CORS** (Cross-Origin Resource Sharing) is configured globally in the API Gateway:

```properties
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=http://localhost:4200,http://localhost:8080
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
```

**Why**: Browsers block HTTP requests from a different **origin** (protocol+domain+port). Angular at `localhost:4200` making requests to `localhost:8080` is considered cross-origin. The gateway's CORS headers tell the browser: "These requests are permitted."

---

## 6. Resume / Document Upload Pipeline — In-Depth

### 6.1 Database Storage Strategy (LONGBLOB)

The project stores resume files directly in MySQL using a `LONGBLOB` column.

| Approach | Pros | Cons |
|---|---|---|
| **MySQL LONGBLOB (this project)** | Simple, no external storage, transactional | Not scalable for very large files |
| **Filesystem** | Fast, simple | Not portable, disk management needed |
| **Object Storage (S3, GCS)** | Highly scalable | Requires cloud credentials |

**LONGBLOB** can store up to 4 GB. The project enforces a **10 MB limit** at both the application and Spring Multipart layer.

---

### 6.2 Document Entity

```java
@Entity
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;     // Who uploaded this document

    @Column(nullable = false)
    private String fileName;       // Original filename (e.g., "resume.pdf")

    private String contentType;    // MIME type (e.g., "application/pdf")
    private Long fileSize;         // Size in bytes

    @Lob
    @Column(columnDefinition = "LONGBLOB")  // Binary large object in MySQL
    private byte[] fileData;       // Actual file content as byte array

    @JsonIgnore                    // NEVER serialize binary bytes into JSON response
    public byte[] getFileData() { return fileData; }
}
```

**Critical design point**: `@JsonIgnore` on `getFileData()` ensures the binary file content is never accidentally serialized into a JSON response. The binary data is only returned through the dedicated download endpoint.

---

### 6.3 Upload Flow — Step by Step

```
Employee selects file via <input type="file">
          |
candidate.service.ts:
  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post('/api/me/documents', formData);
  }
          |
JWT auto-injected by HTTP Interceptor → Authorization: Bearer <token>
          |
API Gateway → routes to candidate-service:8082
          |
CandidateController @PostMapping("/api/me/documents"):
  1. Extract JWT → get employeeId
  2. If null → return 401 Unauthorized
  3. candidateService.uploadDocument(empId, file)
          |
CandidateService.uploadDocument():
  1. file null or empty → throw "File is empty or missing!"
  2. file.getSize() > 10MB → throw "File size exceeds 10MB limit!"
  3. Extract: contentType, fileName
  4. Create Document entity with file.getBytes() as fileData
  5. documentRepository.save(doc) → persisted to MySQL LONGBLOB
  6. doc.setFileData(null) → clear binary from in-memory response
  7. Return metadata (id, fileName, contentType, fileSize, uploadedAt)
          |
Angular receives: { id: 5, fileName: "resume.pdf", contentType: "application/pdf", fileSize: 84321 }
```

---

### 6.4 Download Flow — Step by Step

```
User clicks "Download Resume" button
          |
Angular opens URL in new tab:
  window.open(candidateService.getDownloadUrl(documentId))

getDownloadUrl(id): string {
  const token = localStorage.getItem('ijp_token');
  return `/api/documents/${id}/download?token=${encodeURIComponent(token)}`;
}
          |
GET /api/documents/5/download?token=eyJhbGciOi...
API Gateway → routes to candidate-service:8082
          |
CandidateController:
  1. Accept JWT from Authorization header OR ?token= query param
  2. Extract employeeId + role
  3. boolean isHr = role is "ADMIN"
  4. candidateService.getDocumentById(id, empId, isHr)
          |
CandidateService.getDocumentById():
  1. documentRepository.findById(id) → loads full Document including fileData
  2. If NOT HR AND doc.employeeId != requestingEmployeeId → throw Unauthorized
  3. Return document (with fileData populated)
          |
Controller builds HTTP response:
  return ResponseEntity.ok()
    .contentType(MediaType.parseMediaType("application/pdf"))
    .header(Content-Disposition, "attachment; filename=\"resume.pdf\"")
    .body(doc.getFileData());  // Raw bytes as HTTP body
          |
Browser auto-downloads "resume.pdf"
```

---

### 6.5 Authorization Logic for Downloads

| Requester | Can Download | Logic |
|---|---|---|
| **Admin / HR** | Any document | `isHr = true` → no ownership check |
| **Employee (own doc)** | Own documents only | `doc.employeeId == requestingEmployeeId` |
| **Employee (other's doc)** | Blocked | 403 Forbidden |
| **No token** | Blocked | 403 Forbidden |

---

### 6.6 Why the Download URL Uses a Query Token

Standard browser download links (opened via `window.open()`) cannot have custom HTTP headers. The `Authorization: Bearer <token>` header cannot be injected into browser navigation.

**Solution**: Pass the JWT as `?token=...`. The backend accepts from **either** the Authorization header or the query parameter:

```java
String token = authHeader;
if ((token == null || token.trim().isEmpty()) && tokenParam != null) {
    token = "Bearer " + tokenParam;  // Reconstruct expected format
}
```

`encodeURIComponent` is used in Angular because JWT tokens contain `.` and possibly `+`, `=` characters that could break URL query string parsing.

---

## 7. Application Lifecycle State Machine

### 7.1 States Explained

```
SUBMITTED → UNDER_REVIEW → SHORTLISTED → INTERVIEW_SCHEDULED → SELECTED
                        ↘ REJECTED (terminal)              ↘ REJECTED (terminal)

At any non-terminal state:
→ WITHDRAWN (by employee, terminal)
```

### 7.2 State Transition Rules

| Valid Transition | Who Triggers |
|---|---|
| SUBMITTED → UNDER_REVIEW | HR Admin |
| UNDER_REVIEW → SHORTLISTED | HR Admin |
| UNDER_REVIEW → REJECTED | HR Admin |
| SHORTLISTED → INTERVIEW_SCHEDULED | HR Admin |
| INTERVIEW_SCHEDULED → SELECTED | HR Admin |
| INTERVIEW_SCHEDULED → REJECTED | HR Admin |
| Any non-terminal → WITHDRAWN | Employee only |

---

### 7.3 Withdrawal Logic — Key Business Rule

```java
public Application withdrawApplication(Long applicationId, String employeeId) {
    Application app = applicationRepository.findById(applicationId).orElseThrow(...);

    // Ownership check
    if (!app.getEmployeeId().equalsIgnoreCase(employeeId)) {
        throw new RuntimeException("Unauthorized to withdraw this application.");
    }

    // Cannot withdraw after final decision
    if ("SELECTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {
        throw new RuntimeException("Cannot withdraw application after terminal decision.");
    }

    // Cannot withdraw an already withdrawn application
    if ("WITHDRAWN".equalsIgnoreCase(currentStatus)) {
        throw new RuntimeException("Application is already withdrawn.");
    }

    app.setStatus("WITHDRAWN");
    return applicationRepository.save(app);
}
```

**Critical bug fix**: HR Admin can no longer perform stage updates (UNDER_REVIEW, SHORTLIST, etc.) on withdrawn applications:

```java
public Application updateApplicationStageByHR(Long applicationId, String newStage, ...) {
    Application app = applicationRepository.findById(applicationId).orElseThrow(...);

    // Guard: Withdrawn applications are frozen
    if ("WITHDRAWN".equalsIgnoreCase(app.getStatus())) {
        throw new RuntimeException("Cannot update stage: Candidate has WITHDRAWN this application.");
    }
    // ...rest of logic
}
```

---

### 7.4 Duplicate Application Prevention

```java
// Check for existing active applications (excluding withdrawn or rejected)
List<Application> activeApps = applicationRepository.findByEmployeeIdAndJobIdAndStatusNotIn(
    profile.getEmployeeId(), jobId, List.of("WITHDRAWN", "REJECTED")
);

if (!activeApps.isEmpty()) {
    throw new RuntimeException("You already have an active application submitted for this job.");
}
```

After withdrawing or being rejected, an employee **can re-apply** — only truly active applications block duplicate submissions.

---

## 8. Inter-Service Communication — OpenFeign

**File**: `JobServiceClient.java`

```java
@FeignClient(name = "job-service")  // Looks up "JOB-SERVICE" in Eureka
public interface JobServiceClient {
    @GetMapping("/api/jobs/{id}")
    JobPostingDto getJobById(@PathVariable("id") Long id);
}
```

**How it works**:
1. `@FeignClient(name = "job-service")` tells Feign to discover the service through Eureka
2. Feign generates a proxy at runtime that makes HTTP GET to `http://localhost:8081/api/jobs/{id}`
3. Deserializes JSON response into `JobPostingDto`
4. `@EnableFeignClients` in `CandidateServiceApplication.java` activates this

**Why needed**: When a candidate applies, the candidate-service needs to verify the job exists, is still OPEN/PUBLISHED, and capture the job's title, designation, and location for the application snapshot.

---

## 9. Database Design

### 9.1 employee_profile Table

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment internal ID |
| `employeeId` | VARCHAR UNIQUE | Company ID (e.g., EMP1001) |
| `email` | VARCHAR UNIQUE | Must end with @company.com |
| `password` | VARCHAR | BCrypt-hashed |
| `firstName` | VARCHAR | Letters/spaces/hyphens only |
| `lastName` | VARCHAR | Same validation |
| `dob` | VARCHAR | Stored as YYYY-MM-DD string |
| `designation` | VARCHAR | Current job role |
| `department` | VARCHAR | Department |
| `skills` | VARCHAR | Comma-separated skills |
| `experienceYears` | DOUBLE | Bounded 0–60 |
| `createdAt` | DATETIME | Account creation time |

**Design note**: `dob` is stored as a `String` rather than `DATE` to avoid timezone conversion issues and to enforce exact format validation in Java using `LocalDate.parse()`.

---

### 9.2 application Table

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Primary key |
| `employeeId` | VARCHAR | References employee |
| `jobId` | BIGINT | References job posting |
| `jobSnapshotTitle` | VARCHAR | **Immutable copy** of job title at apply time |
| `jobSnapshotDesignation` | VARCHAR | Immutable copy |
| `jobSnapshotLocation` | VARCHAR | Immutable copy |
| `jobSnapshotDepartment` | VARCHAR | Immutable copy |
| `coverNote` | TEXT | Candidate's cover note |
| `documentId` | BIGINT | FK to document.id (resume) |
| `status` | VARCHAR | Application stage |
| `reviewerNotes` | TEXT | **Private HR notes** — stripped from employee responses |
| `submittedAt` | DATETIME | Submission timestamp |
| `updatedAt` | DATETIME | Auto-updated on status change |

**Why Job Snapshot?** Job details can change after submission (job closed, designation renamed). By copying into the application record at submission time, the record always reflects what the candidate applied for.

---

### 9.3 document Table

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Primary key |
| `employeeId` | VARCHAR | Who uploaded this |
| `fileName` | VARCHAR | Original file name |
| `contentType` | VARCHAR | MIME type |
| `fileSize` | BIGINT | Bytes |
| `fileData` | LONGBLOB | **Binary file content** |
| `uploadedAt` | DATETIME | Upload timestamp |

---

### 9.4 interview Table

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Primary key |
| `applicationId` | BIGINT | Links to application |
| `interviewMode` | VARCHAR | `ONLINE` or `OFFLINE` |
| `interviewDate` | VARCHAR | Date string |
| `interviewTime` | VARCHAR | Time string |
| `location` | VARCHAR | Room name (OFFLINE) |
| `meetingLink` | VARCHAR | Meeting URL (ONLINE) |
| `interviewer` | VARCHAR | Interviewer name |
| `status` | VARCHAR | `SCHEDULED`, `COMPLETED`, `CANCELLED` |
| `feedbackNotes` | TEXT | Post-interview feedback |

---

### 9.5 notification Table

| Column | Description |
|---|---|
| `employeeId` | Recipient |
| `title` | Notification heading |
| `message` | Full message body |
| `type` | SUBMITTED, SHORTLISTED, etc. |
| `read` | Boolean — shown as unread badge if false |

---

### 9.6 job_posting Table (in job_db)

| Column | Description |
|---|---|
| `jobId` | Business ID (JOB-101) |
| `title` | Job title |
| `description` | Full job description |
| `designation` | Target designation |
| `salaryMin` / `salaryMax` | Bounded salary range |
| `status` | PUBLISHED / OPEN / DRAFT / CLOSED |

---

### 9.7 designation Table (in admin_db)

| Column | Description |
|---|---|
| `name` | Designation name (unique) |
| `status` | ACTIVE or INACTIVE |

---

## 10. Validation Framework — End-to-End

### 10.1 Backend Service Validation (Java)

**`validateCandidateProfileInput()` in CandidateService**:

```java
// Name: only letters, spaces, hyphens, apostrophes
if (!firstName.trim().matches("^[a-zA-Z\\s'-]+$"))
    throw new RuntimeException("First Name must contain only alphabetic characters!");

// Employee ID: alphanumeric, underscore, hyphen
if (!employeeId.trim().matches("^[A-Za-z0-9_-]+$"))
    throw new RuntimeException("Employee ID contains invalid characters!");

// Email: must contain @ AND end with @company.com
if (!email.trim().contains("@")) throw new RuntimeException("Invalid email format!");
if (!email.trim().endsWith("@company.com")) throw new RuntimeException("Only @company.com emails allowed.");

// Password minimum 6 characters
if (password.trim().length() < 6) throw new RuntimeException("Password must be at least 6 characters long!");

// Experience: bounded 0-60 years
if (experienceYears < 0.0 || experienceYears > 60.0)
    throw new RuntimeException("Experience years must be between 0 and 60!");

// DOB: format + year + not future + minimum age 18
LocalDate birthDate = LocalDate.parse(dob.trim());  // Throws if not YYYY-MM-DD
if (birthDate.getYear() < 1900) throw new RuntimeException("Birth year must be 1900 or later!");
if (birthDate.isAfter(LocalDate.now())) throw new RuntimeException("DOB cannot be in the future!");
if (Period.between(birthDate, LocalDate.now()).getYears() < 18)
    throw new RuntimeException("Candidate must be at least 18 years old!");
```

`java.time.Period.between()` computes exact age handling edge cases like "birthday not yet passed this year."

---

### 10.2 Frontend Form Validation (Angular)

```html
<!-- Name field with regex pattern -->
<input type="text" name="firstName"
       [(ngModel)]="candidate.firstName"
       pattern="^[a-zA-Z\s'-]+$"
       required />
<div *ngIf="firstName.invalid && firstName.touched">
  Letters, spaces, hyphens, and apostrophes only.
</div>

<!-- DOB with max date enforcing 18+ age -->
<input type="date" name="dob"
       [(ngModel)]="candidate.dob"
       [max]="maxDobDate"
       min="1900-01-01"
       required />

<!-- Password minimum length -->
<input type="password" name="password"
       [(ngModel)]="candidate.password"
       minlength="6"
       required />
```

**`maxDobDate` computed in TypeScript**:
```typescript
const today = new Date();
const minAgeDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
this.maxDobDate = minAgeDate.toISOString().split('T')[0];  // "YYYY-MM-DD"
```

---

## 11. Notification System

Notifications are automatically created by the backend at key lifecycle events.

| Event | Notification Title | Type |
|---|---|---|
| Application submitted | "Application Submitted" | SUBMITTED |
| Application withdrawn | "Application Withdrawn" | WITHDRAWN |
| Stage → SHORTLISTED | "Application Shortlisted" | SHORTLISTED |
| Stage → SELECTED | "Application Selected" | SELECTED |
| Stage → REJECTED | "Application Rejected" | REJECTED |
| Interview scheduled | "Interview Scheduled" | INTERVIEW_SCHEDULED |

```java
private Notification createNotification(String employeeId, String title, String message, String type) {
    Notification notif = new Notification(null, employeeId, title, message, type, false, null);
    return notificationRepository.save(notif);
}
```

The employee dashboard shows unread notifications with a badge count. Clicking marks them as read via `PATCH /api/me/notifications/{id}/read`.

---

## 12. Angular Frontend — Architecture & Key Features

### 12.1 Component Tree

```
AppComponent (root)
├── HomeComponent — Public job listing
├── LoginComponent — Employee and Admin login
├── RegisterComponent — Employee registration with validation
├── EmployeeDashboardComponent
│   ├── My Applications (with status color badges)
│   ├── Notifications bell
│   └── Profile section
├── ProfileComponent — Resume upload and profile edit
├── ApplyComponent (modal) — Job application with document selector
├── AdminDashboardComponent
│   ├── Job Management (create/close/view)
│   ├── Designation Management
│   └── HR Applications Panel
└── ViewCandidatesComponent — HR review, stage update, interview scheduling
```

---

### 12.2 HTTP Interceptor (JWT Injection)

Angular's `HttpInterceptor` automatically adds the JWT to every outgoing HTTP request:

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.getToken();

        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`  // Auto-added to every API call
                }
            });
        }

        return next.handle(request);
    }
}
```

**Why**: Without an interceptor, every service method would need to manually add `Authorization` headers. The interceptor applies it globally.

---

### 12.3 Route Guards (RBAC)

```typescript
// employee.guard.ts
export const employeeGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn() && authService.getRole() === 'EMPLOYEE') {
        return true;
    }

    router.navigate(['/login']);
    return false;
};

// Applied in router:
{ path: 'employee-dashboard', component: EmployeeDashboardComponent, canActivate: [employeeGuard] },
{ path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [adminGuard] },
```

---

### 12.4 AuthService — localStorage Token Management

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
    login(token: string, role: string): void {
        localStorage.setItem('ijp_token', token);
        localStorage.setItem('ijp_role', role);
    }

    logout(): void {
        localStorage.removeItem('ijp_token');
        localStorage.removeItem('ijp_role');
    }

    getToken(): string | null { return localStorage.getItem('ijp_token'); }
    isLoggedIn(): boolean { return !!this.getToken(); }

    // Decode JWT payload without calling backend (Base64 decode)
    getEmployeeId(): string {
        const token = this.getToken();
        if (!token) return '';
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.employeeId || '';
    }
}
```

`token.split('.')[1]` extracts the Base64-encoded payload. `atob()` decodes it. `JSON.parse()` gives access to all JWT claims locally without a network call.

---

## 13. Unit Testing Strategy

### 13.1 Testing Approach

Tests use **JUnit 5 + Mockito** for the service layer. No actual database — repositories are **mocked**.

```java
@BeforeEach
public void setup() {
    MockitoAnnotations.openMocks(this);  // Initialize @Mock and @InjectMocks
}

// Arrange → Act → Assert pattern:
when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(job);  // Arrange
JobPosting created = jobPostingService.createJob(job);                    // Act
assertNotNull(created);                                                   // Assert
assertEquals("OPEN", created.getStatus());
verify(jobPostingRepository, times(1)).save(job);                        // Verify interaction
```

---

### 13.2 Negative Testing Categories

| Category | What is Tested | Example |
|---|---|---|
| **Null Input** | Null object passed to service | `createJob(null)` |
| **Blank/Empty** | Required fields empty or whitespace | `title = ""` |
| **Malformed Regex** | Values failing pattern validation | `firstName = "John123"` |
| **Boundary Value** | Values at valid/invalid boundary | `salaryMin = -1`, `experience = 65.0` |
| **Invalid Date** | Wrong format, future dates, underage | `dob = "not-a-date"`, `dob = "2099-01-01"` |
| **Missing Entity** | Querying non-existent records | `getJobById(99L)` when no job 99 |
| **State Machine** | Actions on wrong state | HR update on WITHDRAWN application |
| **Duplicate** | Creating duplicate resources | Duplicate designation name |
| **Domain Constraint** | Business-specific rules | Email not ending with @company.com |

---

### 13.3 Test Summary Table

| Microservice | Test Class | Total Tests | Positive | Negative |
|---|---|---|---|---|
| Job Service | `JobPostingServiceTest` | 17 | 4 | 13 |
| Candidate Service | `CandidateServiceTest` | 20 | 4 | 16 |
| Admin Service | `AdminServiceTest` | 13 | 2 | 11 |
| **Total** | — | **50** | **10** | **40** |

---

## 14. Add-On: Security Best Practices Implemented

| Practice | Implementation |
|---|---|
| **Password Hashing** | BCrypt cost factor 10 — irreversible, salted |
| **Token Expiry** | JWT expires after 24 hours |
| **Ownership Verification** | Document downloads check requesting user owns the document |
| **Input Sanitization** | All string inputs `.trim()`-ed before use or validation |
| **Regex Validation** | Pattern enforcement prevents injection-prone characters in names and IDs |
| **Role Enforcement** | Backend (JWT role claim) AND frontend (route guards) both enforce RBAC |
| **CORS Whitelist** | Only `localhost:4200` whitelisted as allowed origin |
| **No Binary in JSON** | `@JsonIgnore` on `fileData` prevents accidental blob serialization |
| **Private HR Notes** | `reviewerNotes` stripped from employee responses: `app.setReviewerNotes(null)` |
| **State Machine Guards** | Withdrawn applications blocked from further HR updates |
| **File Size Limit** | 10 MB cap at Spring Multipart config AND service layer |
| **Email Domain Lock** | Only `@company.com` emails accepted for login and registration |

---

## 15. API Endpoint Reference

### Candidate Service (via Gateway :8080)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Employee registration |
| POST | `/api/auth/login` | None | Employee login → JWT |
| GET | `/api/me` | Employee JWT | Current user profile |
| POST | `/api/applications` | Employee JWT | Submit job application |
| GET | `/api/applications/me` | Employee JWT | My applications |
| PATCH | `/api/applications/{id}/withdraw` | Employee JWT | Withdraw application |
| POST | `/api/me/documents` | Employee JWT | Upload resume (multipart) |
| GET | `/api/me/documents` | Employee JWT | List my documents |
| GET | `/api/documents/{id}/download` | JWT (header or ?token=) | Download resume |
| GET | `/api/me/notifications` | Employee JWT | My notifications |
| PATCH | `/api/me/notifications/{id}/read` | Employee JWT | Mark read |
| GET | `/api/hr/applications` | Admin JWT | All applications |
| PATCH | `/api/hr/applications/{id}/stage` | Admin JWT | Update application stage |
| POST | `/api/hr/applications/{id}/interviews` | Admin JWT | Schedule interview |

### Job Service (via Gateway :8080)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/jobs/open` | None | Published/open jobs |
| GET | `/api/jobs/{id}` | None | Get job by ID |
| POST | `/api/jobs` | Admin JWT | Create job posting |
| PUT | `/api/jobs/{id}` | Admin JWT | Update job |
| PATCH | `/api/jobs/{id}/close` | Admin JWT | Close a job |

### Admin Service (via Gateway :8080)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/admin/login` | None | Admin login → JWT |
| GET | `/api/admin/designations` | None | All designations |
| POST | `/api/admin/designations` | Admin JWT | Create designation |
| PUT | `/api/admin/designations/{id}` | Admin JWT | Update designation |
| PATCH | `/api/admin/designations/{id}/status` | Admin JWT | Toggle ACTIVE/INACTIVE |

---

## 16. How to Start the Project

Run services in this exact order:

```bash
# 1. Start Eureka Service Discovery
cd service-discovery && mvn spring-boot:run
# Verify: http://localhost:8761 — Eureka dashboard

# 2. Start API Gateway
cd api-gateway && mvn spring-boot:run

# 3. Start microservices (order among these doesn't matter)
cd job-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run
cd candidate-service && mvn spring-boot:run

# 4. Start Angular frontend
cd ijp-frontend
npm install
npm start
# Opens: http://localhost:4200
```

**Test Credentials**:
- Employee: `john.doe@company.com` / `password123`
- Admin: `admin@company.com` / `admin123`

---

## 17. Viva Preparation — Core Concepts Q&A

**Q: What is the difference between authentication and authorization?**
Authentication verifies *who you are* (login → JWT issued). Authorization determines *what you can do* (JWT role claim checked to allow or deny operations).

**Q: Why use JWT instead of sessions?**
JWT is stateless — the server doesn't store any session data. In a microservices system, sessions would require shared storage (Redis). JWT embeds all claims in the token; any service can verify it independently using the shared secret key.

**Q: What happens if someone tampers with the JWT payload?**
The token has a cryptographic signature (HMAC-SHA256). If the payload changes even by one bit, the signature becomes invalid. JJWT throws a `SignatureException`, and `validateToken()` returns `false`.

**Q: Why is BCrypt better than MD5 or SHA-256 for passwords?**
MD5 and SHA256 are fast — attackers can compute billions of hashes per second. BCrypt is intentionally slow (cost factor 10 ≈ 100ms per hash) and includes a random salt, making precomputed rainbow table attacks useless.

**Q: What is `@Lob` and `LONGBLOB`?**
`@Lob` (Large Object) tells JPA to map a field to a large binary/text column. `LONGBLOB` is a MySQL type storing up to 4 GB of binary data. We use it to store resume file bytes directly in MySQL.

**Q: What is `@JsonIgnore` and why is it on `getFileData()`?**
It tells Jackson (JSON serialization library) to exclude this field from JSON responses. Without it, the binary bytes would be Base64-encoded into the JSON — creating huge, unusable payloads. Binary data is only returned through the dedicated download endpoint as raw HTTP bytes.

**Q: What is a Job Snapshot and why do you store it?**
When an employee applies, we copy the job's title, designation, location, and department into the Application record. This ensures the application always reflects what the employee applied for, even if the job is later edited or deleted.

**Q: How does the document download work through a browser URL if JWT can't be sent as a header?**
Browser navigation cannot carry custom headers. So the JWT is passed as a URL query parameter `?token=...`. The backend accepts it from either the Authorization header or the token query parameter.

**Q: What is OpenFeign?**
A declarative HTTP client. You define an interface annotated with `@FeignClient` and HTTP mapping annotations. Feign generates the actual HTTP implementation at runtime, integrating with Eureka for service discovery.

**Q: What is the Application State Machine?**
A set of defined states (SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED, WITHDRAWN) with defined valid transitions. The backend enforces only valid transitions and blocks operations on terminal states.

**Q: How does CORS work in this project?**
CORS is configured in the API Gateway for all routes, allowing requests from `localhost:4200`. The gateway adds `Access-Control-Allow-Origin` response headers that tell the browser the cross-origin request is permitted.

**Q: What does `@PostConstruct` do?**
The annotated method runs once after Spring finishes injecting all dependencies into the bean. We use it to seed test data (sample employees, jobs, designations) when the database is empty.

**Q: What is the difference between `@Mock` and `@InjectMocks` in unit tests?**
`@Mock` creates a Mockito proxy that does nothing by default but can be configured with `when(...).thenReturn(...)`. `@InjectMocks` creates an actual instance of the class under test and injects all `@Mock` objects into it, simulating Spring dependency injection.

**Q: Why does the application validate on both frontend AND backend?**
Frontend validation (Angular form patterns) gives immediate user feedback without a network round-trip — better UX. Backend validation is the security enforcement layer that cannot be bypassed even if someone crafts raw HTTP requests (e.g., via Postman or curl), skipping the frontend entirely.

**Q: What is `Period.between()` used for?**
`java.time.Period.between(birthDate, LocalDate.now()).getYears()` computes the exact age in years, correctly handling the case where "today is before the birthday this calendar year" — so a person born on Dec 31 2006 would not yet be 18 on Dec 30 2024.

**Q: What is the difference between `PUBLISHED` and `OPEN` job status?**
Both indicate the job is actively accepting applications. `PUBLISHED` is the standard status when an admin publishes a job. `OPEN` is a legacy/alternate status. The system accepts both when checking if a job is open for applications: `if (!\"PUBLISHED\".equals(jobStatus) && !\"OPEN\".equals(jobStatus)) → throw exception`.

**Q: Why does the system use an immutable job snapshot instead of a foreign key join?**
A foreign key join fetches *current* job data. But job postings can be edited, closed, or deleted after applications are submitted. The snapshot preserves historical context — what the candidate saw and applied for — which is critical for audit and compliance.
