# Internal Job Posting (IJP) System — Comprehensive Technical Documentation & Viva Guide

---

## Table of Contents

1. [Project Introduction & Purpose](#1-project-introduction--purpose)
2. [System Architecture Overview](#2-system-architecture-overview)
3. [Technology Stack](#3-technology-stack)
4. [Microservices — Detailed Breakdown](#4-microservices--detailed-breakdown)
5. [Security — Spring Security, BCrypt & JWT In-Depth](#5-security--spring-security-bcrypt--jwt-in-depth)
6. [Resume / Document Upload Pipeline](#6-resume--document-upload-pipeline)
7. [Application Lifecycle State Machine](#7-application-lifecycle-state-machine)
8. [Inter-Service Communication — OpenFeign](#8-inter-service-communication--openfeign)
9. [Database Design](#9-database-design)
10. [Validation Framework — End-to-End](#10-validation-framework--end-to-end)
11. [Notification System](#11-notification-system)
12. [Angular Frontend — Architecture & UI System](#12-angular-frontend--architecture--ui-system)
13. [Unit Testing Strategy](#13-unit-testing-strategy)
14. [Security Best Practices Implemented](#14-security-best-practices-implemented)
15. [API Endpoint Reference](#15-api-endpoint-reference)
16. [How to Start the Project](#16-how-to-start-the-project)
17. [Viva Preparation — Core Concepts Q&A](#17-viva-preparation--core-concepts-qa)

---

## 1. Project Introduction & Purpose

The **Internal Job Posting (IJP) System** is a full-stack microservices-based web application that facilitates transparent internal job mobility for employees across an organization. It allows employees to discover career opportunities, track application progress, and manage resumes, while enabling HR Admins to post job requisitions, review applications, and schedule candidate interviews.

### Problem Statement
In traditional corporate environments, internal recruitment relies on informal email chains or physical notice boards. This lack of structure leads to untracked applications, delayed hiring pipelines, and low transparency. The IJP System digitizes and automates the internal hiring lifecycle.

### Key Stakeholders
| Actor | Role in System |
|---|---|
| **Employee (Candidate)** | Registers account, browses published roles, applies with cover notes and resumes, tracks application stages, receives notifications |
| **HR Admin** | Manages designation master, posts job requisitions, reviews candidate applications, updates stages, schedules online/offline interviews |
| **System Gateway & Microservices** | Enforces security rules, validates inputs, generates sequential job codes, routes API traffic, sends in-app notifications |

### Core System Features
- **Employee Registration & Authentication**: Secure JWT-based registration with optional Last Name, 18–80 DOB age validation, `@company.com` email domain restriction, and industry-standard password policies (8+ chars with uppercase, lowercase, digit, and special characters).
- **Job Requisition Management**: Automated sequential Job Code generation (`JOB-101`, `JOB-102`...), designation and role title typeahead suggestions, dynamic skill pills, character counter for descriptions, salary range validation, and deadline selectors.
- **Candidate Job Application**: Duplicate application prevention per job, auto-populated profile metadata, cover note submission, and uploaded resume document linking.
- **Application Stage State Machine**: 7-stage status workflow (`SUBMITTED` → `UNDER_REVIEW` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED` / `REJECTED` / `WITHDRAWN`).
- **HR Application Review & Interview Scheduling**: HR pipeline view, stage transition controls with private reviewer notes, online meeting link generator, and physical location room assigner.
- **About Us & Career Mobility Guide (`/about`)**: Dedicated portal page outlining internal transfer guidelines, tenure criteria (min 6 months), 4-step application process, and HR desk contacts.
- **Design System & UI**: Custom dark slate (`#0f172a`) and indigo (`#4f46e5`) styling, interactive password strength meter, responsive navbar, and footer.

---

## 2. System Architecture Overview

The system follows a **Cloud-Native Microservices Architecture** where microservices register with a central service discovery registry and handle API requests via a unified API Gateway.

```
Browser (Angular SPA)
        |
   localhost:4200
        |
        ▼
  [API Gateway] <----> [Eureka Service Registry]
   localhost:8080            localhost:8761
        |
   +----+----+----+
   |         |    |
   v         v    v
[Candidate] [Job] [Admin]
 Service   Service Service
  :8082     :8081   :8083
   |         |       |
 MySQL     MySQL   MySQL
candidate  job_db  admin_db
```

### Architectural Highlights
1. **API Gateway (`port 8080`)**: Single entry point for frontend traffic, managing global CORS policies and routing `/api/jobs/**`, `/api/applications/**`, and `/api/admin/**`.
2. **Eureka Service Discovery (`port 8761`)**: Enables dynamic load balancing and service lookup without hardcoded host IP mappings.
3. **OpenFeign Client**: Declarative REST client used by Candidate Service to fetch job snapshot details from Job Service during application submission.
4. **Stateless JWT Security**: Passports candidate and admin identities securely across microservice boundaries.

---

## 3. Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Backend** | Java 17, Spring Boot 3.x, Spring Cloud | Core microservices framework |
| **Service Discovery** | Spring Cloud Netflix Eureka | Dynamic service registry |
| **API Gateway** | Spring Cloud Gateway | Global routing and CORS handler |
| **Inter-Service Communication** | OpenFeign | Declarative REST client |
| **Database** | MySQL 8.x, Spring Data JPA / Hibernate | Relational persistence and BLOB storage |
| **Security** | Spring Security, BCrypt, JWT | Password hashing & token authentication |
| **Frontend** | Angular 17, TypeScript, HTML5, CSS3 | Single Page Application UI |
| **Testing** | JUnit 5, Mockito, Spring Boot Test | Unit & service layer testing |

---

## 4. Microservices Breakdown

### 4.1 Service Discovery (Eureka Server)
* **Port**: `8761`
* **Role**: Central lookup directory for all active microservices. Listens for periodic heartbeat signals to track active service instances.

### 4.2 API Gateway
* **Port**: `8080`
* **Role**: Intercepts browser requests, resolves target microservice locations via Eureka, and enforces cross-origin resource sharing (CORS) for `http://localhost:4200`.

### 4.3 Job Service
* **Port**: `8081`
* **Role**: Manages internal job postings, status lifecycles (`PUBLISHED`, `DRAFT`, `PAUSED`, `CLOSED`), automated sequential code assignment (`JOB-101`, `JOB-102`...), and multi-criteria role filtering (designation, location, skills).

### 4.4 Candidate Service
* **Port**: `8082`
* **Role**: Handles candidate registration, authentication, employee profile management, application submission, duplicate active application prevention, status stage progression, resume BLOB upload/download, and candidate notifications.

### 4.5 Admin Service
* **Port**: `8083`
* **Role**: Handles HR Admin authentication, role separation, and Designation Master management (creating, updating, and toggling active designation statuses).

---

## 5. Security — Spring Security, BCrypt & JWT

1. **Password Hashing**: Employee passwords are encrypted using `BCryptPasswordEncoder` with a default strength work factor of 10 prior to persistence.
2. **JWT Structure**: Signed JSON Web Tokens containing claims for `sub` (email), `role` (`EMPLOYEE` / `ADMIN`), `employeeId`, `id`, and expiration timestamps.
3. **Role Separation**: Admin credentials cannot authenticate through employee login routes, and employees cannot access admin endpoints.

---

## 6. Resume / Document Upload Pipeline

* **Storage**: Uploaded PDF/DOCX resumes are stored directly in MySQL as binary data (`LONGBLOB`) inside the `documents` table.
* **Security**: Downloads require valid JWT authorization. Document ownership is enforced so candidates can only access their own documents, while HR Admins have administrative review access.

---

## 7. Application Lifecycle State Machine

```
[SUBMITTED] ---> [UNDER_REVIEW] ---> [SHORTLISTED] ---> [INTERVIEW_SCHEDULED] ---> [SELECTED]
      |                                  |
      +----------------------------------+----------------------------------------> [REJECTED]
      |
      v
 [WITHDRAWN] (Terminal candidate state)
```

1. **SUBMITTED**: Candidate submits job application.
2. **UNDER_REVIEW**: HR inspects candidate qualifications and cover note.
3. **SHORTLISTED**: Candidate passes initial screening.
4. **INTERVIEW_SCHEDULED**: HR assigns interview date, time, mode (Online link / Offline room).
5. **SELECTED / REJECTED**: Terminal decision stage.
6. **WITHDRAWN**: Candidate cancels active application before terminal decision.

---

## 8. End-to-End Validation Rules

### Employee Registration
* **First Name**: Mandatory, alphabetic characters, spaces, hyphens, and apostrophes only.
* **Last Name**: Optional. If provided, pattern validated.
* **Employee ID**: Mandatory, alphanumeric characters, hyphens, and underscores only.
* **Date of Birth**: Mandatory, age must be between 18 and 80 years old.
* **Company Email**: Mandatory, ending with `@company.com`. Admin email registration blocked.
* **Password**: Mandatory, minimum 8 characters with at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character.
* **Confirm Password**: Must match password exactly.

### Job Posting Requisition
* **Job Code**: System-generated sequential string (`JOB-101`, `JOB-102`...).
* **Posting Title & Designation**: Mandatory, minimum 3 characters.
* **Department & Work Location**: Mandatory.
* **Skill Set**: Mandatory, comma-separated format.
* **Job Description**: Mandatory, minimum 20 characters.
* **Salary Range**: Non-negative, Maximum Salary $\ge$ Minimum Salary.
* **Application Deadline**: Date must be today or a future date.

---

## 9. Unit Testing Summary

All unit tests across microservices execute cleanly with **100% pass rate**:

* **Job Service (`JobPostingServiceTest`)**: 17 Test Cases (0 Failures)
* **Candidate Service (`CandidateServiceTest`)**: 20 Test Cases (0 Failures)
* **Admin Service (`AdminServiceTest`)**: 13 Test Cases (0 Failures)
* **Total**: **50 Unit Tests Passing**

---

## 10. How to Start the System

1. **Start Eureka Service Discovery**:
   ```bash
   cd service-discovery
   mvn spring-boot:run
   ```
2. **Start Backend Microservices**:
   ```bash
   cd admin-service && mvn spring-boot:run
   cd candidate-service && mvn spring-boot:run
   cd job-service && mvn spring-boot:run
   cd api-gateway && mvn spring-boot:run
   ```
3. **Start Angular Frontend**:
   ```bash
   cd ijp-frontend
   npm start
   ```
4. **Access Web Application**:
   Open browser at [http://localhost:4200](http://localhost:4200)
