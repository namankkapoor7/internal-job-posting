# Internal Job Posting (IJP) System — Unit Testing Report

| Microservice | Test Class Name | Total Test Cases | Positive Tests | Negative / Boundary Tests | Result Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Job Service** | `JobPostingServiceTest` | 17 | 4 | 13 | **PASSED (100%)** |
| **Candidate Service** | `CandidateServiceTest` | 20 | 4 | 16 | **PASSED (100%)** |
| **Admin Service** | `AdminServiceTest` | 13 | 2 | 11 | **PASSED (100%)** |
| **Total Suite** | **3 Microservice Test Classes** | **50** | **10** | **40** | **ALL 50 PASSED** |

---

## 1. Comprehensive Test Case Specifications

### 1.1 JobPostingService Test Suite (17 Tests)

| Test Case Method Name | Test Type | Parameter Input Tested | Expected Exception / Assertion | Result |
| :--- | :--- | :--- | :--- | :--- |
| `testCreateJob_Success()` | Positive | Valid `JobPosting` object | Returns saved job with `OPEN` status | **PASS** |
| `testCreateJob_DefaultStatus()` | Positive | Job with `status = null` | Defaults status to `OPEN` | **PASS** |
| `testCloseJob_Success()` | Positive | Existing Job ID `1L` | Updates status to `CLOSED` | **PASS** |
| `testDeleteJob_Success()` | Positive | Existing Job ID `1L` | Deletes record from repository | **PASS** |
| `testCreateJob_NullInput()` | Null Input | `null` `JobPosting` | `RuntimeException`: *"Job posting object cannot be null!"* | **PASS** |
| `testCreateJob_MissingDesignation()` | Blank Input | `designation = ""` | `RuntimeException`: *"Job designation is required!"* | **PASS** |
| `testCreateJob_MissingTitle()` | Blank Input | `title = ""` | `RuntimeException`: *"Job title is required!"* | **PASS** |
| `testCreateJob_MissingLocation()` | Blank Input | `location = "   "` | `RuntimeException`: *"Job location is required!"* | **PASS** |
| `testCreateJob_MissingDescription()` | Blank Input | `description = ""` | `RuntimeException`: *"Job description is required!"* | **PASS** |
| `testCreateJob_MissingExperience()` | Blank Input | `experience = ""` | `RuntimeException`: *"Job experience requirement is required!"* | **PASS** |
| `testCreateJob_NegativeSalary()` | Boundary Value | `salaryMin = -500.0` | `RuntimeException`: *"Minimum salary cannot be negative!"* | **PASS** |
| `testCreateJob_InvertedSalaryRange()` | Boundary Value | `salaryMin = 90000.0`, `salaryMax = 50000.0` | `RuntimeException`: *"Maximum salary cannot be less than minimum salary!"* | **PASS** |
| `testUpdateJob_NegativeSalary()` | Boundary Value | `salaryMin = -1000.0` | `RuntimeException`: *"Minimum salary cannot be negative!"* | **PASS** |
| `testUpdateJob_InvertedSalaryRange()` | Boundary Value | `salaryMin = 100000.0`, `salaryMax = 40000.0` | `RuntimeException`: *"Maximum salary cannot be less than minimum salary!"* | **PASS** |
| `testDeleteJob_NotFound()` | Missing Entity | Non-existent Job ID `99L` | `RuntimeException`: *"Job posting with ID 99 not found!"* | **PASS** |
| `testUpdateJob_NotFound()` | Missing Entity | Non-existent Job ID `99L` | `RuntimeException`: *"Job posting with ID 99 not found!"* | **PASS** |
| `testUpdateJob_NullId()` | Null Input | `id = null` | `RuntimeException`: *"Job ID cannot be null!"* | **PASS** |

---

### 1.2 CandidateService Test Suite (20 Tests)

| Test Case Method Name | Test Type | Parameter Input Tested | Expected Exception / Assertion | Result |
| :--- | :--- | :--- | :--- | :--- |
| `testRegisterEmployee_Success()` | Positive | Valid `EmployeeProfile` with password `Password123!` | Successfully registers candidate | **PASS** |
| `testLoginEmployee_Success()` | Positive | Valid credentials `Password123!` | Returns valid JWT token & profile | **PASS** |
| `testApplyForJob_Success()` | Positive | Valid Emp ID & Published Job ID | Creates application with `SUBMITTED` status | **PASS** |
| `testWithdrawApplication_Success()` | Positive | Active Application ID `10L` | Updates status to `WITHDRAWN` | **PASS** |
| `testRegisterEmployee_NullProfile()` | Null Input | `null` `EmployeeProfile` | `RuntimeException`: *"Employee profile object cannot be null!"* | **PASS** |
| `testRegisterEmployee_MissingFirstName()` | Blank Input | `firstName = ""` | `RuntimeException`: *"First Name is required!"* | **PASS** |
| `testRegisterEmployee_FirstNameWithNumbers()` | Regex Error | `firstName = "John123"` | `RuntimeException`: *"First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"* | **PASS** |
| `testRegisterEmployee_LastNameWithSpecialChars()` | Regex Error | `lastName = "Doe@#$"` | `RuntimeException`: *"Last Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"* | **PASS** |
| `testRegisterEmployee_ShortPassword()` | Password Policy | `password = "123"` | `RuntimeException`: *"Password must be at least 8 characters long!"* | **PASS** |
| `testRegisterEmployee_InvalidEmployeeIdFormat()` | Regex Error | `employeeId = "EMP 101!"` | `RuntimeException`: *"Employee ID must contain only alphanumeric characters, underscores, or hyphens!"* | **PASS** |
| `testRegisterEmployee_MalformedEmail()` | Format Error | `email = "notanemail"` | `RuntimeException`: *"Invalid email format!"* | **PASS** |
| `testRegisterEmployee_InvalidEmailDomain()` | Domain Rule | `email = "john@gmail.com"` | `RuntimeException`: *"Only company email addresses ending with @company.com are allowed."* | **PASS** |
| `testRegisterEmployee_FutureDob()` | Date Boundary | `dob = "2099-01-01"` | `RuntimeException`: *"Date of Birth cannot be in the future!"* | **PASS** |
| `testRegisterEmployee_UnderageDob()` | Age Boundary | `dob = "2020-01-01"` | `RuntimeException`: *"Candidate must be at least 18 years old!"* | **PASS** |
| `testRegisterEmployee_NegativeExperience()` | Range Boundary | `experienceYears = -2.5` | `RuntimeException`: *"Experience years must be between 0 and 60!"* | **PASS** |
| `testRegisterEmployee_ExcessiveExperience()` | Range Boundary | `experienceYears = 65.0` | `RuntimeException`: *"Experience years must be between 0 and 60!"* | **PASS** |
| `testUpdateProfile_InvalidFirstName()` | Regex Error | `firstName = "John999"` | `RuntimeException`: *"First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!"* | **PASS** |
| `testApplyForJob_ClosedJob()` | State Machine | Application to `CLOSED` job | `RuntimeException`: *"Cannot apply: Job posting 'Java Developer' is CLOSED!"* | **PASS** |
| `testUpdateStage_WithdrawnApplication()` | State Machine | Stage update on `WITHDRAWN` app | `RuntimeException`: *"Cannot update stage: Candidate has WITHDRAWN this application."* | **PASS** |
| `testScheduleInterview_WithdrawnApplication()` | State Machine | Schedule interview on `WITHDRAWN` app | `RuntimeException`: *"Cannot schedule interview: Candidate has WITHDRAWN this application."* | **PASS** |

---

### 1.3 AdminService Test Suite (13 Tests)

| Test Case Method Name | Test Type | Parameter Input Tested | Expected Exception / Assertion | Result |
| :--- | :--- | :--- | :--- | :--- |
| `testValidateLogin_Success()` | Positive | Valid Admin credentials | Returns `true` | **PASS** |
| `testAddDesignation_Success()` | Positive | Valid Designation object | Saves new designation | **PASS** |
| `testValidateLogin_NullEmail()` | Null Input | `email = null` | `RuntimeException`: *"Email is required!"* | **PASS** |
| `testValidateLogin_NullPassword()` | Null Input | `password = null` | `RuntimeException`: *"Password is required!"* | **PASS** |
| `testValidateLogin_MalformedEmail()` | Format Error | `email = "notanemail"` | `RuntimeException`: *"Invalid email format!"* | **PASS** |
| `testValidateLogin_InvalidEmailDomain()` | Domain Rule | `email = "admin@gmail.com"` | `RuntimeException`: *"Only company email addresses ending with @company.com are allowed."* | **PASS** |
| `testAddDesignation_NullDesignation()` | Null Input | `null` Designation | `RuntimeException`: *"Designation object cannot be null!"* | **PASS** |
| `testAddDesignation_EmptyName()` | Blank Input | `name = "   "` | `RuntimeException`: *"Designation name is required!"* | **PASS** |
| `testAddDesignation_TooShortName()` | Length Boundary | `name = "A"` | `RuntimeException`: *"Designation name must be at least 2 characters long!"* | **PASS** |
| `testAddDesignation_InvalidCharacters()` | Regex Error | `name = "Developer<Script>"` | `RuntimeException`: *"Designation name contains invalid characters!"* | **PASS** |
| `testAddDesignation_DuplicateName()` | Duplicate Rule | Existing `"QA Engineer"` | `RuntimeException`: *"Designation 'QA Engineer' already exists!"* | **PASS** |
| `testUpdateDesignation_NotFound()` | Missing Entity | Non-existent ID `99L` | `RuntimeException`: *"Designation with ID 99 not found!"* | **PASS** |
| `testUpdateDesignation_InvalidCharacters()` | Regex Error | `name = "QA Lead @#$"` | `RuntimeException`: *"Designation name contains invalid characters!"* | **PASS** |
