# UNIT TESTING REPORT - SERVICE LAYER DEEP NEGATIVE TESTING

| Microservice | Test Class Name | Total Test Cases | Positive Tests | Negative / Boundary Tests |
| :--- | :--- | :--- | :--- | :--- |
| Job Service | JobPostingServiceTest | 11 | 4 | 7 |
| Candidate Service | CandidateServiceTest | 13 | 4 | 9 |
| Admin Service | AdminServiceTest | 10 | 2 | 8 |
| **Total** | **3 Test Classes** | **34** | **10** | **24** |

---

## 1. Test Cases Specification

### 1.1 JobPostingService Test Cases

| Test Case Method Name | Category / Test Type | Parameter Input Tested | Expected Exception Class & Message | Side-Effect Assertion |
| :--- | :--- | :--- | :--- | :--- |
| `testCreateJob_Success()` | Positive | Valid JobPosting object | None (`OPEN` status returned) | `verify(repo, times(1)).save(job)` |
| `testCreateJob_DefaultStatus()` | Positive | JobPosting with `status = null` | None (`OPEN` status defaulted) | `verify(repo, times(1)).save(job)` |
| `testCloseJob_Success()` | Positive | Existing Job ID `1L` | None (`CLOSED` status updated) | `verify(repo, times(1)).save(job)` |
| `testDeleteJob_Success()` | Positive | Existing Job ID `1L` | None (Deleted) | `verify(repo, times(1)).deleteById(1L)` |
| `testCreateJob_NullInput()` | Null Input | `null` JobPosting object | `RuntimeException`: `"Job posting object cannot be null!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_MissingDesignation()` | Blank Input | `designation = ""` | `RuntimeException`: `"Job designation is required!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_NegativeSalary()` | Boundary Value | `salaryMin = -500.0` | `RuntimeException`: `"Minimum salary cannot be negative!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_InvertedSalaryRange()` | Boundary Value | `salaryMin = 90000.0`, `salaryMax = 50000.0` | `RuntimeException`: `"Maximum salary cannot be less than minimum salary!"` | `verify(repo, never()).save(any())` |
| `testDeleteJob_NotFound()` | Missing Entity | Non-existent Job ID `99L` | `RuntimeException`: `"Job posting with ID 99 not found!"` | `verify(repo, never()).deleteById(any())` |
| `testUpdateJob_NotFound()` | Missing Entity | Non-existent Job ID `99L` | `RuntimeException`: `"Job posting with ID 99 not found!"` | `verify(repo, never()).save(any())` |
| `testUpdateJob_NullId()` | Null Input | `id = null` | `RuntimeException`: `"Job ID cannot be null!"` | `verify(repo, never()).save(any())` |

---

### 1.2 CandidateService Test Cases

| Test Case Method Name | Category / Test Type | Parameter Input Tested | Expected Exception Class & Message | Side-Effect Assertion |
| :--- | :--- | :--- | :--- | :--- |
| `testRegisterEmployee_Success()` | Positive | Valid EmployeeProfile | None (Registered) | `verify(repo, times(1)).save(profile)` |
| `testLoginEmployee_Success()` | Positive | Valid Email & Password | None (JWT token returned) | None |
| `testApplyForJob_Success()` | Positive | Valid Emp ID & Job ID | None (`SUBMITTED` status returned) | `verify(notifRepo, times(1)).save(any())` |
| `testWithdrawApplication_Success()` | Positive | Active Application ID `10L` | None (`WITHDRAWN` status updated) | `verify(notifRepo, times(1)).save(any())` |
| `testRegisterEmployee_NullProfile()` | Null Input | `null` EmployeeProfile object | `RuntimeException`: `"Employee profile object cannot be null!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_MissingFirstName()` | Blank Input | `firstName = ""` | `RuntimeException`: `"First Name is required!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_MalformedEmail()` | Malformed Input | `email = "notanemail"` | `RuntimeException`: `"Invalid email format!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_InvalidEmailDomain()` | Boundary / Domain | `email = "john@gmail.com"` | `RuntimeException`: `"Only company email addresses ending with @company.com are allowed."` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_NegativeExperience()` | Boundary Value | `experienceYears = -2.5` | `RuntimeException`: `"Experience years cannot be negative!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_InvalidDobFormat()` | Malformed Input | `dob = "not-a-date"` | `RuntimeException`: `"Invalid Date of Birth format! Must be YYYY-MM-DD."` | `verify(repo, never()).save(any())` |
| `testApplyForJob_ClosedJob()` | State Boundary | Application to `CLOSED` job | `RuntimeException`: `"Cannot apply: Job posting 'Java Developer' is CLOSED!"` | `verify(appRepo, never()).save(any())` |
| `testUpdateStage_WithdrawnApplication()` | State Boundary | Stage update on `WITHDRAWN` app | `RuntimeException`: `"Cannot update stage: Candidate has WITHDRAWN this application."` | `verify(appRepo, never()).save(any())` |
| `testScheduleInterview_WithdrawnApplication()` | State Boundary | Interview on `WITHDRAWN` app | `RuntimeException`: `"Cannot schedule interview: Candidate has WITHDRAWN this application."` | `verify(interviewRepo, never()).save(any())` |

---

### 1.3 AdminService Test Cases

| Test Case Method Name | Category / Test Type | Parameter Input Tested | Expected Exception Class & Message | Side-Effect Assertion |
| :--- | :--- | :--- | :--- | :--- |
| `testValidateLogin_Success()` | Positive | Valid Admin credentials | None (`true` returned) | `verify(repo, times(1)).findByEmail(any())` |
| `testAddDesignation_Success()` | Positive | Valid Designation object | None (Saved) | `verify(desigRepo, times(1)).save(any())` |
| `testValidateLogin_NullEmail()` | Null Input | `email = null` | `RuntimeException`: `"Email is required!"` | `verify(repo, never()).findByEmail(any())` |
| `testValidateLogin_NullPassword()` | Null Input | `password = null` | `RuntimeException`: `"Password is required!"` | `verify(repo, never()).findByEmail(any())` |
| `testValidateLogin_MalformedEmail()` | Malformed Input | `email = "notanemail"` | `RuntimeException`: `"Invalid email format!"` | `verify(repo, never()).findByEmail(any())` |
| `testValidateLogin_InvalidEmailDomain()` | Domain Boundary | `email = "admin@gmail.com"` | `RuntimeException`: `"Only company email addresses ending with @company.com are allowed."` | `verify(repo, never()).findByEmail(any())` |
| `testAddDesignation_NullDesignation()` | Null Input | `null` Designation object | `RuntimeException`: `"Designation object cannot be null!"` | `verify(desigRepo, never()).save(any())` |
| `testAddDesignation_EmptyName()` | Blank Input | `name = "   "` | `RuntimeException`: `"Designation name is required!"` | `verify(desigRepo, never()).save(any())` |
| `testAddDesignation_DuplicateName()` | Duplicate Boundary | Existing name `"QA Engineer"` | `RuntimeException`: `"Designation 'QA Engineer' already exists!"` | `verify(desigRepo, never()).save(any())` |
| `testUpdateDesignation_NotFound()` | Missing Entity | Non-existent ID `99L` | `RuntimeException`: `"Designation with ID 99 not found!"` | `verify(desigRepo, never()).save(any())` |

---

## 2. Test Failure & System Improvement Case Studies

This section documents three concrete scenarios where unit testing identified missing validation or edge-case flaws in the application, leading to direct code fixes in the backend services and frontend user interface.

### Case Study 1: Job Posting Null & Boundary Salary Validation

* **Encountered Failure**: When executing `testCreateJob_NullInput()` and `testCreateJob_NegativeSalary()`, the service method originally attempted to access fields on `null` or store negative salary figures (`-500.0`) directly into MySQL without validation, causing unhandled `NullPointerException` and corrupted DB state.
* **Service Code Fix ([JobPostingService.java](file:///d:/internal-job-posting/job-service/src/main/java/com/example/jobservice/service/JobPostingService.java#L43-L55))**:
  ```java
  if (jobPosting == null) {
      throw new RuntimeException("Job posting object cannot be null!");
  }
  if (jobPosting.getDesignation() == null || jobPosting.getDesignation().trim().isEmpty()) {
      throw new RuntimeException("Job designation is required!");
  }
  if (jobPosting.getSalaryMin() != null && jobPosting.getSalaryMin() < 0) {
      throw new RuntimeException("Minimum salary cannot be negative!");
  }
  if (jobPosting.getSalaryMin() != null && jobPosting.getSalaryMax() != null && jobPosting.getSalaryMax() < jobPosting.getSalaryMin()) {
      throw new RuntimeException("Maximum salary cannot be less than minimum salary!");
  }
  ```
* **Result**: Unit tests pass 100%, and side-effect checks confirm `verify(jobPostingRepository, never()).save(any())` is strictly respected.

---

### Case Study 2: Candidate Registration Email & Date Formatting Validation

* **Encountered Failure**: When executing `testRegisterEmployee_MalformedEmail()` and `testRegisterEmployee_InvalidDobFormat()`, inputs like `"notanemail"` or plain text dates `"not-a-date"` passed initial checks and were saved to the repository.
* **Service Code Fix ([CandidateService.java](file:///d:/internal-job-posting/candidate-service/src/main/java/com/example/candidateservice/service/CandidateService.java#L80-L95))**:
  ```java
  if (!email.contains("@")) {
      throw new RuntimeException("Invalid email format!");
  }
  if (!email.endsWith("@company.com")) {
      throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
  }
  if (profile.getExperienceYears() != null && profile.getExperienceYears() < 0) {
      throw new RuntimeException("Experience years cannot be negative!");
  }
  if (profile.getDob() != null && !profile.getDob().trim().isEmpty()) {
      if (!profile.getDob().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
          throw new RuntimeException("Invalid Date of Birth format! Must be YYYY-MM-DD.");
      }
  }
  ```
* **Result**: Invalid registration attempts are cleanly rejected before hitting persistence layers.

---

### Case Study 3: State Machine Enforcement for Withdrawn Applications

* **Encountered Failure**: When testing HR workflow actions on withdrawn applications (`testUpdateStage_WithdrawnApplication()` and `testScheduleInterview_WithdrawnApplication()`), HR Admins were previously able to change stages or schedule interviews for candidates who had already withdrawn their applications.
* **Service & Frontend Fix**:
  1. **Backend Validation ([CandidateService.java](file:///d:/internal-job-posting/candidate-service/src/main/java/com/example/candidateservice/service/CandidateService.java#L329-L332))**:
     ```java
     if ("WITHDRAWN".equalsIgnoreCase(app.getStatus())) {
         throw new RuntimeException("Cannot update stage: Candidate has WITHDRAWN this application.");
     }
     ```
  2. **Frontend UI Fix ([view-candidates.component.html](file:///d:/internal-job-posting/ijp-frontend/src/app/components/view-candidates/view-candidates.component.html#L86-L102))**:
     Stage dropdown, schedule interview button, and reviewer notes inputs are automatically disabled when `app.status === 'WITHDRAWN'`.
* **Result**: State integrity is strictly preserved across both backend microservices and Angular client views.
