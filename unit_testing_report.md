# UNIT TESTING REPORT - SERVICE LAYER DEEP NEGATIVE TESTING

| Microservice | Test Class Name | Total Test Cases | Positive Tests | Negative / Boundary Tests |
| :--- | :--- | :--- | :--- | :--- |
| Job Service | JobPostingServiceTest | 17 | 4 | 13 |
| Candidate Service | CandidateServiceTest | 20 | 4 | 16 |
| Admin Service | AdminServiceTest | 13 | 2 | 11 |
| **Total** | **3 Test Classes** | **50** | **10** | **40** |

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
| `testCreateJob_MissingTitle()` | Blank Input | `title = ""` | `RuntimeException`: `"Job title is required!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_MissingLocation()` | Blank Input | `location = "   "` | `RuntimeException`: `"Job location is required!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_MissingDescription()` | Blank Input | `description = ""` | `RuntimeException`: `"Job description is required!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_MissingExperience()` | Blank Input | `experience = ""` | `RuntimeException`: `"Job experience requirement is required!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_NegativeSalary()` | Boundary Value | `salaryMin = -500.0` | `RuntimeException`: `"Minimum salary cannot be negative!"` | `verify(repo, never()).save(any())` |
| `testCreateJob_InvertedSalaryRange()` | Boundary Value | `salaryMin = 90000.0`, `salaryMax = 50000.0` | `RuntimeException`: `"Maximum salary cannot be less than minimum salary!"` | `verify(repo, never()).save(any())` |
| `testUpdateJob_NegativeSalary()` | Boundary Value | `salaryMin = -1000.0` | `RuntimeException`: `"Minimum salary cannot be negative!"` | `verify(repo, never()).save(any())` |
| `testUpdateJob_InvertedSalaryRange()` | Boundary Value | `salaryMin = 100000.0`, `salaryMax = 40000.0` | `RuntimeException`: `"Maximum salary cannot be less than minimum salary!"` | `verify(repo, never()).save(any())` |
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
| `testRegisterEmployee_FirstNameWithNumbers()` | Malformed Regex | `firstName = "John123"` | `RuntimeException`: `"First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_LastNameWithSpecialChars()` | Malformed Regex | `lastName = "Doe@#$"` | `RuntimeException`: `"Last Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_ShortPassword()` | Boundary Length | `password = "123"` | `RuntimeException`: `"Password must be at least 6 characters long!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_InvalidEmployeeIdFormat()` | Malformed Regex | `employeeId = "EMP 101!"` | `RuntimeException`: `"Employee ID must contain only alphanumeric characters, underscores, or hyphens!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_MalformedEmail()` | Malformed Input | `email = "notanemail"` | `RuntimeException`: `"Invalid email format!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_InvalidEmailDomain()` | Boundary / Domain | `email = "john@gmail.com"` | `RuntimeException`: `"Only company email addresses ending with @company.com are allowed."` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_FutureDob()` | Boundary Date | `dob = "2099-01-01"` | `RuntimeException`: `"Date of Birth cannot be in the future!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_UnderageDob()` | Age Boundary | `dob = "2020-01-01"` | `RuntimeException`: `"Candidate must be at least 18 years old!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_NegativeExperience()` | Boundary Value | `experienceYears = -2.5` | `RuntimeException`: `"Experience years must be between 0 and 60!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_ExcessiveExperience()` | Boundary Value | `experienceYears = 65.0` | `RuntimeException`: `"Experience years must be between 0 and 60!"` | `verify(repo, never()).save(any())` |
| `testRegisterEmployee_InvalidDobFormat()` | Malformed Input | `dob = "not-a-date"` | `RuntimeException`: `"Invalid Date of Birth format! Must be YYYY-MM-DD."` | `verify(repo, never()).save(any())` |
| `testUpdateProfile_InvalidFirstName()` | Malformed Regex | `firstName = "John999"` | `RuntimeException`: `"First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"` | `verify(repo, never()).save(any())` |
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
| `testAddDesignation_TooShortName()` | Length Boundary | `name = "A"` | `RuntimeException`: `"Designation name must be at least 2 characters long!"` | `verify(desigRepo, never()).save(any())` |
| `testAddDesignation_InvalidCharacters()` | Malformed Regex | `name = "Developer<Script>"` | `RuntimeException`: `"Designation name contains invalid characters!"` | `verify(desigRepo, never()).save(any())` |
| `testAddDesignation_DuplicateName()` | Duplicate Boundary | Existing name `"QA Engineer"` | `RuntimeException`: `"Designation 'QA Engineer' already exists!"` | `verify(desigRepo, never()).save(any())` |
| `testUpdateDesignation_NotFound()` | Missing Entity | Non-existent ID `99L` | `RuntimeException`: `"Designation with ID 99 not found!"` | `verify(desigRepo, never()).save(any())` |
| `testUpdateDesignation_InvalidCharacters()` | Malformed Regex | `name = "QA Lead @#$"` | `RuntimeException`: `"Designation name contains invalid characters!"` | `verify(desigRepo, never()).save(any())` |

---

## 2. Test Failure & System Improvement Case Studies

This section documents four concrete scenarios where unit testing identified missing validation or edge-case flaws in the application, leading to direct code fixes in the backend services and frontend user interface.

### Case Study 1: Job Posting Null & Boundary Salary Validation

* **Encountered Failure**: When executing `testCreateJob_NullInput()` and `testCreateJob_NegativeSalary()`, the service method originally attempted to access fields on `null` or store negative salary figures (`-500.0`) directly into MySQL without validation, causing unhandled `NullPointerException` and corrupted DB state.
* **Service Code Fix ([JobPostingService.java](file:///d:/internal-job-posting/job-service/src/main/java/com/example/jobservice/service/JobPostingService.java#L43-L60))**:
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

---

### Case Study 4: End-to-End Name, Age & Designation Format Validation

* **Encountered Failure**: Evaluators identified that users could register candidate profiles containing digits or special symbols (e.g. `John123`), enter underage dates of birth, or create designation names containing illegal characters (e.g. `<Script>`), which was allowed by basic string checks.
* **Service & Frontend Fix**:
  1. **Candidate Profile Validation ([CandidateService.java](file:///d:/internal-job-posting/candidate-service/src/main/java/com/example/candidateservice/service/CandidateService.java#L80-L120))**:
     Applied regex `^[a-zA-Z\s'-]+$` for names, parsed DOB to calculate age (`Period.between(dob, now).getYears() >= 18`), checked password length $\ge 6$, and capped experience between $0.0$ and $60.0$.
  2. **Designation Master Validation ([AdminService.java](file:///d:/internal-job-posting/admin-service/src/main/java/com/example/adminservice/service/AdminService.java#L78-L88))**:
     Enforced length $\ge 2$ and regex pattern `^[a-zA-Z0-9\s&/-]+$`.
  3. **Angular Form Controls ([register.component.html](file:///d:/internal-job-posting/ijp-frontend/src/app/components/register/register.component.html#L22-L60))**:
     Enforced HTML5 regex pattern attributes, max DOB date calculation (`[max]="maxDobDate"`), and real-time inline badge warnings.
* **Result**: End-to-end validation guarantees clean data entry from the browser form down to MySQL table storage.
