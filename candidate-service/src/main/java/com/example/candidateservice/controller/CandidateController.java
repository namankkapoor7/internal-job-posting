package com.example.candidateservice.controller;

import com.example.candidateservice.entity.*;
import com.example.candidateservice.security.JwtUtil;
import com.example.candidateservice.service.CandidateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper to extract JWT token from request header
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private String getEmployeeIdFromToken(String authHeader) {
        String token = extractToken(authHeader);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractEmployeeId(token);
        }
        return null;
    }

    private String getRoleFromToken(String authHeader) {
        String token = extractToken(authHeader);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractRole(token);
        }
        return null;
    }

    // =========================================================
    // AUTHENTICATION & PROFILES
    // =========================================================

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginReq) {
        try {
            String email = loginReq != null ? loginReq.get("email") : null;
            String password = loginReq != null ? loginReq.get("password") : null;

            Map<String, Object> response = candidateService.loginEmployee(email, password);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody EmployeeProfile profile) {
        try {
            EmployeeProfile saved = candidateService.registerEmployee(profile);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // Legacy Auth Routes
    @PostMapping("/api/candidates/login")
    public ResponseEntity<?> legacyLogin(@RequestBody Map<String, String> loginReq) {
        return login(loginReq);
    }

    @PostMapping("/api/candidates/register")
    public ResponseEntity<?> legacyRegister(@RequestBody EmployeeProfile profile) {
        return register(profile);
    }

    @GetMapping("/api/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request or missing Bearer token");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        Optional<EmployeeProfile> profileOpt = candidateService.getProfileByEmployeeId(empId);
        if (profileOpt.isPresent()) {
            return new ResponseEntity<>(profileOpt.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    // APPLICATIONS
    // =========================================================

    @PostMapping("/api/applications")
    public ResponseEntity<?> submitApplication(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> reqBody) {

        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null && reqBody.containsKey("employeeId")) {
            empId = (String) reqBody.get("employeeId");
        }

        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized application attempt.");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        try {
            Long jobId = reqBody.get("jobId") instanceof Number ? ((Number) reqBody.get("jobId")).longValue() : Long.parseLong(reqBody.get("jobId").toString());
            String coverNote = (String) reqBody.get("coverNote");
            Long documentId = reqBody.get("documentId") != null ? Long.parseLong(reqBody.get("documentId").toString()) : null;

            Application app = candidateService.applyForJob(empId, jobId, coverNote, documentId);
            return new ResponseEntity<>(app, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/applications/me")
    public ResponseEntity<?> getMyApplications(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        List<Application> apps = candidateService.getApplicationsForEmployee(empId);
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    @PatchMapping("/api/applications/{id}/withdraw")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        try {
            Application withdrawn = candidateService.withdrawApplication(id, empId);
            return new ResponseEntity<>(withdrawn, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // Legacy Apply Route
    @PostMapping("/api/candidates")
    public ResponseEntity<?> legacyApply(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader, @RequestBody Map<String, Object> body) {
        return submitApplication(authHeader, body);
    }

    // =========================================================
    // HR APPLICATION REVIEW & INTERVIEWS
    // =========================================================

    @GetMapping("/api/hr/applications")
    public ResponseEntity<?> getHRApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> apps = candidateService.getAllApplicationsForHR(jobId, status);
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    @PatchMapping("/api/hr/applications/{id}/stage")
    public ResponseEntity<?> updateApplicationStage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            String stage = body != null ? body.get("stage") : null;
            if (stage == null && body != null) stage = body.get("status");
            String reviewerNotes = body != null ? body.get("reviewerNotes") : null;

            Application updated = candidateService.updateApplicationStageByHR(id, stage, reviewerNotes);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/hr/applications/{id}/interviews")
    public ResponseEntity<?> scheduleInterviewForApp(
            @PathVariable Long id,
            @RequestBody Interview interview) {

        try {
            interview.setApplicationId(id);
            Interview scheduled = candidateService.scheduleInterview(interview);
            return new ResponseEntity<>(scheduled, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/candidates/interviews")
    public ResponseEntity<?> legacyScheduleInterview(@RequestBody Interview interview) {
        try {
            Interview scheduled = candidateService.scheduleInterview(interview);
            return new ResponseEntity<>(scheduled, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/hr/interviews")
    public ResponseEntity<List<Interview>> getAllHRInterviews() {
        return new ResponseEntity<>(candidateService.getAllInterviews(), HttpStatus.OK);
    }

    @GetMapping("/api/candidates/interviews/candidate/{id}")
    public ResponseEntity<List<Interview>> legacyGetInterviews(@PathVariable String id) {
        return new ResponseEntity<>(candidateService.getInterviewsByEmployeeId(id), HttpStatus.OK);
    }

    // =========================================================
    // DOCUMENTS
    // =========================================================

    @PostMapping("/api/me/documents")
    public ResponseEntity<?> uploadDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file) {

        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized document upload");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        try {
            Document doc = candidateService.uploadDocument(empId, file);
            // Return document without binary bytes payload
            doc.setFileData(null);
            return new ResponseEntity<>(doc, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/me/documents")
    public ResponseEntity<?> getMyDocuments(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        List<Document> docs = candidateService.getDocumentsForEmployee(empId);
        docs.forEach(doc -> doc.setFileData(null));
        return new ResponseEntity<>(docs, HttpStatus.OK);
    }

    @GetMapping("/api/documents/{id}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long id,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String token = authHeader;
        if ((token == null || token.trim().isEmpty()) && tokenParam != null && !tokenParam.trim().isEmpty()) {
            token = "Bearer " + tokenParam;
        }

        String empId = getEmployeeIdFromToken(token);
        String role = getRoleFromToken(token);
        boolean isHr = "ADMIN".equalsIgnoreCase(role) || "HR_ADMIN".equalsIgnoreCase(role);

        try {
            Document doc = candidateService.getDocumentById(id, empId, isHr);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType() != null ? doc.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                    .body(doc.getFileData());
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    @GetMapping("/api/me/notifications")
    public ResponseEntity<?> getMyNotifications(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        List<Notification> list = candidateService.getNotificationsForEmployee(empId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PatchMapping("/api/me/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String empId = getEmployeeIdFromToken(authHeader);
        if (empId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized request");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        try {
            Notification updated = candidateService.markNotificationAsRead(id, empId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    // Legacy Notifications Routes
    @GetMapping("/api/candidates/notifications/employee/{identifier}")
    public ResponseEntity<List<Notification>> legacyGetNotifications(@PathVariable String identifier) {
        return new ResponseEntity<>(candidateService.getNotificationsForEmployee(identifier), HttpStatus.OK);
    }

    @PutMapping("/api/candidates/notifications/{id}/read")
    public ResponseEntity<?> legacyMarkRead(@PathVariable Long id) {
        try {
            Notification notif = candidateService.markNotificationAsRead(id, "EMP1001");
            return new ResponseEntity<>(notif, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }
}