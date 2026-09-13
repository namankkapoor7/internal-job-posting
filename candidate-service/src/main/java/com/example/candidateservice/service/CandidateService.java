package com.example.candidateservice.service;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.*;
import com.example.candidateservice.repository.*;
import com.example.candidateservice.security.JwtUtil;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class CandidateService {

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void initSampleEmployees() {
        if (employeeProfileRepository.count() == 0) {
            EmployeeProfile emp1 = new EmployeeProfile(
                null, "EMP1001", "john.doe@company.com", passwordEncoder.encode("password123"),
                "John", "Doe", "1992-05-15", "Java Full Stack Developer", "Engineering", "Java, Spring Boot, Angular", 4.5
            );
            EmployeeProfile emp2 = new EmployeeProfile(
                null, "EMP1002", "jane.smith@company.com", passwordEncoder.encode("password123"),
                "Jane", "Smith", "1994-08-20", "Angular Developer", "UX Design", "Angular, TypeScript, HTML/CSS", 3.0
            );
            employeeProfileRepository.saveAll(List.of(emp1, emp2));
        }
    }

    // =========================================================
    // AUTHENTICATION & EMPLOYEE PROFILE
    // =========================================================

    public EmployeeProfile registerEmployee(EmployeeProfile profile) {
        if (profile == null) {
            throw new RuntimeException("Employee profile object cannot be null!");
        }
        if (profile.getFirstName() == null || profile.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First Name is required!");
        }
        if (profile.getLastName() == null || profile.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last Name is required!");
        }
        if (profile.getEmployeeId() == null || profile.getEmployeeId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required!");
        }
        if (profile.getEmail() == null || profile.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Company Email is required!");
        }
        if (profile.getPassword() == null || profile.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required!");
        }

        String email = profile.getEmail().trim().toLowerCase();
        String empId = profile.getEmployeeId().trim().toUpperCase();

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

        if (employeeProfileRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new RuntimeException("An employee with email '" + email + "' already exists.");
        }

        if (employeeProfileRepository.findByEmployeeId(empId).isPresent()) {
            throw new RuntimeException("An employee with Employee ID '" + empId + "' already exists.");
        }

        profile.setEmail(email);
        profile.setEmployeeId(empId);
        profile.setFirstName(profile.getFirstName().trim());
        profile.setLastName(profile.getLastName().trim());
        profile.setPassword(passwordEncoder.encode(profile.getPassword().trim()));

        return employeeProfileRepository.save(profile);
    }

    public Map<String, Object> loginEmployee(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Company Email is required!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required!");
        }

        String emailTrimmed = email.trim().toLowerCase();
        if (!emailTrimmed.endsWith("@company.com")) {
            throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
        }

        Optional<EmployeeProfile> profileOpt = employeeProfileRepository.findByEmailIgnoreCase(emailTrimmed);
        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password.");
        }

        EmployeeProfile profile = profileOpt.get();
        if (!passwordEncoder.matches(password.trim(), profile.getPassword()) && !password.trim().equals(profile.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(
            profile.getEmail(),
            "EMPLOYEE",
            profile.getEmployeeId(),
            profile.getId(),
            profile.getFirstName() + " " + profile.getLastName()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", profile.getId());
        response.put("employeeId", profile.getEmployeeId());
        response.put("email", profile.getEmail());
        response.put("firstName", profile.getFirstName());
        response.put("lastName", profile.getLastName());
        response.put("designation", profile.getDesignation());
        response.put("department", profile.getDepartment());
        response.put("role", "EMPLOYEE");

        return response;
    }

    public Optional<EmployeeProfile> getProfileByEmployeeId(String employeeId) {
        return employeeProfileRepository.findByEmployeeId(employeeId);
    }

    public Optional<EmployeeProfile> getProfileByEmail(String email) {
        return employeeProfileRepository.findByEmailIgnoreCase(email);
    }

    public EmployeeProfile updateProfile(String employeeId, EmployeeProfile updated) {
        EmployeeProfile profile = employeeProfileRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for ID: " + employeeId));

        if (updated.getFirstName() != null) profile.setFirstName(updated.getFirstName().trim());
        if (updated.getLastName() != null) profile.setLastName(updated.getLastName().trim());
        if (updated.getDob() != null) profile.setDob(updated.getDob().trim());
        if (updated.getDesignation() != null) profile.setDesignation(updated.getDesignation().trim());
        if (updated.getDepartment() != null) profile.setDepartment(updated.getDepartment().trim());
        if (updated.getSkills() != null) profile.setSkills(updated.getSkills().trim());
        if (updated.getExperienceYears() != null) profile.setExperienceYears(updated.getExperienceYears());

        return employeeProfileRepository.save(profile);
    }

    // =========================================================
    // APPLICATION LIFECYCLE & DUPLICATE PREVENTION
    // =========================================================

    public Application applyForJob(String employeeId, Long jobId, String coverNote, Long documentId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required!");
        }
        if (jobId == null || jobId <= 0) {
            throw new RuntimeException("Valid Job ID is required!");
        }

        EmployeeProfile profile = employeeProfileRepository.findByEmployeeId(employeeId.trim())
                .orElseThrow(() -> new RuntimeException("Employee with ID " + employeeId + " not found. Please log in again."));

        // Fetch Job Details from Job Microservice
        JobPostingDto job = null;
        try {
            job = jobServiceClient.getJobById(jobId);
        } catch (Exception e) {
            throw new RuntimeException("Job with ID " + jobId + " does not exist!");
        }

        if (job == null) {
            throw new RuntimeException("Job with ID " + jobId + " does not exist!");
        }

        String jobStatus = job.getStatus() != null ? job.getStatus().toUpperCase() : "CLOSED";
        if (!"PUBLISHED".equals(jobStatus) && !"OPEN".equals(jobStatus)) {
            throw new RuntimeException("Cannot apply: Job posting '" + job.getDesignation() + "' is " + jobStatus + "!");
        }

        // Duplicate Active Application Check
        List<Application> activeApps = applicationRepository.findByEmployeeIdAndJobIdAndStatusNotIn(
                profile.getEmployeeId(), jobId, List.of("WITHDRAWN", "REJECTED")
        );

        if (!activeApps.isEmpty()) {
            throw new RuntimeException("You already have an active application submitted for this job.");
        }

        // Create Immutable Job Snapshot
        Application app = new Application(
            null,
            profile.getEmployeeId(),
            jobId,
            job.getTitle() != null ? job.getTitle() : job.getDesignation(),
            job.getDesignation(),
            job.getLocation(),
            job.getDepartment(),
            coverNote,
            documentId
        );

        Application savedApp = applicationRepository.save(app);

        // Send Notification to Employee
        createNotification(
            profile.getEmployeeId(),
            "Application Submitted",
            "Your application for " + app.getJobSnapshotDesignation() + " (" + app.getJobSnapshotLocation() + ") has been submitted successfully.",
            "SUBMITTED"
        );

        return savedApp;
    }

    public List<Application> getApplicationsForEmployee(String employeeId) {
        List<Application> apps = applicationRepository.findByEmployeeIdOrderBySubmittedAtDesc(employeeId);
        // Exclude private reviewer notes from employee response
        apps.forEach(app -> app.setReviewerNotes(null));
        return apps;
    }

    public Application withdrawApplication(Long applicationId, String employeeId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application ID " + applicationId + " not found!"));

        if (!app.getEmployeeId().equalsIgnoreCase(employeeId)) {
            throw new RuntimeException("Unauthorized to withdraw this application.");
        }

        String currentStatus = app.getStatus();
        if ("SELECTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot withdraw application after terminal decision (" + currentStatus + ").");
        }

        if ("WITHDRAWN".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Application is already withdrawn.");
        }

        app.setStatus("WITHDRAWN");
        Application saved = applicationRepository.save(app);

        createNotification(
            employeeId,
            "Application Withdrawn",
            "Your application for " + app.getJobSnapshotDesignation() + " has been withdrawn.",
            "WITHDRAWN"
        );

        return saved;
    }

    // =========================================================
    // HR APPLICATION REVIEW & STAGE TRANSITIONS
    // =========================================================

    public List<Map<String, Object>> getAllApplicationsForHR(Long jobId, String status) {
        List<Application> apps;
        if (jobId != null && jobId > 0) {
            apps = applicationRepository.findByJobId(jobId);
        } else if (status != null && !status.trim().isEmpty()) {
            apps = applicationRepository.findByStatus(status.trim().toUpperCase());
        } else {
            apps = applicationRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Application app : apps) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("employeeId", app.getEmployeeId());
            map.put("jobId", app.getJobId());
            map.put("jobSnapshotTitle", app.getJobSnapshotTitle());
            map.put("jobSnapshotDesignation", app.getJobSnapshotDesignation());
            map.put("jobSnapshotLocation", app.getJobSnapshotLocation());
            map.put("jobSnapshotDepartment", app.getJobSnapshotDepartment());
            map.put("coverNote", app.getCoverNote());
            map.put("documentId", app.getDocumentId());
            map.put("status", app.getStatus());
            map.put("reviewerNotes", app.getReviewerNotes());
            map.put("submittedAt", app.getSubmittedAt());
            map.put("updatedAt", app.getUpdatedAt());

            // Enrich with Employee Details
            Optional<EmployeeProfile> profileOpt = employeeProfileRepository.findByEmployeeId(app.getEmployeeId());
            if (profileOpt.isPresent()) {
                EmployeeProfile p = profileOpt.get();
                map.put("candidateName", p.getFirstName() + " " + p.getLastName());
                map.put("email", p.getEmail());
                map.put("currentDesignation", p.getDesignation());
                map.put("experienceYears", p.getExperienceYears());
                map.put("skills", p.getSkills());
            } else {
                map.put("candidateName", "Employee (" + app.getEmployeeId() + ")");
            }

            result.add(map);
        }
        return result;
    }

    public Application updateApplicationStageByHR(Long applicationId, String newStage, String reviewerNotes) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application ID " + applicationId + " not found!"));

        if ("WITHDRAWN".equalsIgnoreCase(app.getStatus())) {
            throw new RuntimeException("Cannot update stage: Candidate has WITHDRAWN this application.");
        }

        String stage = newStage != null ? newStage.trim().toUpperCase() : app.getStatus();
        List<String> validStages = List.of("SUBMITTED", "UNDER_REVIEW", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED", "WITHDRAWN");
        if (!validStages.contains(stage)) {
            throw new RuntimeException("Invalid application stage: " + newStage);
        }

        app.setStatus(stage);
        if (reviewerNotes != null) {
            app.setReviewerNotes(reviewerNotes.trim());
        }
        Application saved = applicationRepository.save(app);

        // Notify Employee
        String title = "Application Stage Update";
        String message = "Your application for " + app.getJobSnapshotDesignation() + " is now in stage: " + stage;

        if ("SHORTLISTED".equals(stage)) {
            title = "Application Shortlisted";
            message = "Congratulations! Your application for " + app.getJobSnapshotDesignation() + " has been shortlisted.";
        } else if ("SELECTED".equals(stage)) {
            title = "Application Selected";
            message = "Great news! You have been selected for the position of " + app.getJobSnapshotDesignation() + ".";
        } else if ("REJECTED".equals(stage)) {
            title = "Application Status Update";
            message = "Thank you for your interest. Your application for " + app.getJobSnapshotDesignation() + " was not selected at this time.";
        }

        createNotification(app.getEmployeeId(), title, message, stage);

        return saved;
    }

    // =========================================================
    // INTERVIEWS
    // =========================================================

    public Interview scheduleInterview(Interview interview) {
        if (interview.getApplicationId() == null) {
            throw new RuntimeException("Application ID is required to schedule an interview!");
        }

        Application app = applicationRepository.findById(interview.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application ID " + interview.getApplicationId() + " not found!"));

        if ("WITHDRAWN".equalsIgnoreCase(app.getStatus())) {
            throw new RuntimeException("Cannot schedule interview: Candidate has WITHDRAWN this application.");
        }
        if ("REJECTED".equalsIgnoreCase(app.getStatus())) {
            throw new RuntimeException("Cannot schedule interview: Application status is REJECTED.");
        }

        interview.setEmployeeId(app.getEmployeeId());
        interview.setJobId(app.getJobId());

        String mode = interview.getInterviewMode() != null ? interview.getInterviewMode().toUpperCase() : "OFFLINE";
        interview.setInterviewMode(mode);

        if ("ONLINE".equals(mode)) {
            if (interview.getMeetingLink() == null || interview.getMeetingLink().trim().isEmpty()) {
                throw new RuntimeException("Meeting link is required for ONLINE interview!");
            }
            interview.setLocation(null);
        } else {
            if (interview.getLocation() == null || interview.getLocation().trim().isEmpty()) {
                throw new RuntimeException("Location/Room name is required for OFFLINE interview!");
            }
            interview.setMeetingLink(null);
        }

        interview.setStatus("SCHEDULED");
        Interview savedInterview = interviewRepository.save(interview);

        // Update application status to INTERVIEW_SCHEDULED
        app.setStatus("INTERVIEW_SCHEDULED");
        applicationRepository.save(app);

        // Send notification
        StringBuilder message = new StringBuilder("Interview scheduled for ")
                .append(app.getJobSnapshotDesignation())
                .append(" on ").append(interview.getInterviewDate());
        if (interview.getInterviewTime() != null) message.append(" at ").append(interview.getInterviewTime());
        if ("ONLINE".equals(mode)) {
            message.append(" (ONLINE link: ").append(interview.getMeetingLink()).append(")");
        } else {
            message.append(" (Location: ").append(interview.getLocation()).append(")");
        }

        createNotification(app.getEmployeeId(), "Interview Scheduled", message.toString(), "INTERVIEW_SCHEDULED");

        return savedInterview;
    }

    public List<Interview> getInterviewsByEmployeeId(String employeeId) {
        return interviewRepository.findByEmployeeId(employeeId);
    }

    public List<Interview> getInterviewsByApplicationId(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId);
    }

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    // =========================================================
    // DOCUMENT MANAGEMENT
    // =========================================================

    public Document uploadDocument(String employeeId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or missing!");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10MB limit!");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "document.pdf";

        Document doc = new Document(
            null,
            employeeId,
            originalFilename,
            contentType,
            file.getSize(),
            file.getBytes()
        );

        return documentRepository.save(doc);
    }

    public List<Document> getDocumentsForEmployee(String employeeId) {
        return documentRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
    }

    public Document getDocumentById(Long documentId, String requestingEmployeeId, boolean isHr) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document ID " + documentId + " not found!"));

        if (!isHr && !doc.getEmployeeId().equalsIgnoreCase(requestingEmployeeId)) {
            throw new RuntimeException("Unauthorized access to document ID " + documentId);
        }

        return doc;
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    public Notification createNotification(String employeeId, String title, String message, String type) {
        Notification notif = new Notification(null, employeeId, title, message, type, false, null);
        return notificationRepository.save(notif);
    }

    public List<Notification> getNotificationsForEmployee(String employeeId) {
        return notificationRepository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    public long getUnreadNotificationCount(String employeeId) {
        return notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
    }

    public Notification markNotificationAsRead(Long notificationId, String employeeId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found!"));

        if (!notif.getEmployeeId().equalsIgnoreCase(employeeId)) {
            throw new RuntimeException("Unauthorized action on notification.");
        }

        notif.setRead(true);
        return notificationRepository.save(notif);
    }
}