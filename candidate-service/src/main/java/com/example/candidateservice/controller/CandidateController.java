package com.example.candidateservice.controller;

import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.entity.Interview;
import com.example.candidateservice.entity.Notification;
import com.example.candidateservice.service.CandidateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping("/login")
    public ResponseEntity<?> loginEmployee(
            @RequestBody Map<String, String> loginReq) {

        try {

            String email =
                    loginReq != null
                            ? loginReq.get("email")
                            : null;


            if (email == null ||
                    email.trim().isEmpty()) {

                email =
                        loginReq != null
                                ? loginReq.get("identifier")
                                : null;
            }


            String password =
                    loginReq != null
                            ? loginReq.get("password")
                            : null;


            if (email == null ||
                    email.trim().isEmpty()) {

                throw new RuntimeException(
                        "Company Email is required!"
                );
            }


            if (!email.contains("@")) {

                throw new RuntimeException(
                        "Login requires a valid company email address."
                );
            }


            if (password == null ||
                    password.trim().isEmpty()) {

                throw new RuntimeException(
                        "Invalid email or password."
                );
            }


            Candidate candidate =
                    candidateService.loginEmployee(
                            email.trim(),
                            password.trim()
                    );


            Map<String, Object> response =
                    new HashMap<>();


            response.put(
                    "id",
                    candidate.getId()
            );

            response.put(
                    "employeeId",
                    candidate.getEmployeeId()
            );

            response.put(
                    "firstName",
                    candidate.getFirstName()
            );

            response.put(
                    "lastName",
                    candidate.getLastName()
            );

            response.put(
                    "email",
                    candidate.getEmail()
            );

            response.put(
                    "role",
                    "EMPLOYEE"
            );


            return new ResponseEntity<>(
                    response,
                    HttpStatus.OK
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerEmployee(
            @RequestBody Candidate candidate) {

        try {

            Candidate savedCandidate =
                    candidateService
                            .registerEmployee(candidate);


            return new ResponseEntity<>(
                    savedCandidate,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> applyForJob(
            @RequestBody Candidate candidate) {

        try {

            Candidate savedCandidate =
                    candidateService
                            .applyForJob(candidate);


            return new ResponseEntity<>(
                    savedCandidate,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {

        List<Candidate> candidates =
                candidateService.getAllCandidates();


        return new ResponseEntity<>(
                candidates,
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(
            @PathVariable Long id) {

        Optional<Candidate> candidate =
                candidateService.getCandidateById(id);


        if (candidate.isPresent()) {

            return new ResponseEntity<>(
                    candidate.get(),
                    HttpStatus.OK
            );
        }


        return new ResponseEntity<>(
                HttpStatus.NOT_FOUND
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCandidate(
            @PathVariable Long id) {

        try {

            candidateService.deleteCandidate(id);


            Map<String, String> response =
                    new HashMap<>();


            response.put(
                    "message",
                    "Employee deleted successfully."
            );


            return new ResponseEntity<>(
                    response,
                    HttpStatus.OK
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Candidate>> getCandidatesByEmail(
            @PathVariable String email) {

        List<Candidate> candidates =
                candidateService
                        .getCandidatesByEmail(email);


        return new ResponseEntity<>(
                candidates,
                HttpStatus.OK
        );
    }

    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<List<Candidate>>
    getCandidatesByEmployeeId(
            @PathVariable String employeeId) {

        List<Candidate> candidates =
                candidateService
                        .getCandidatesByEmployeeId(
                                employeeId
                        );


        return new ResponseEntity<>(
                candidates,
                HttpStatus.OK
        );
    }

    @GetMapping("/employee-id/{employeeId}/applications")
    public ResponseEntity<List<Candidate>>
    getApplicationsByEmployeeId(
            @PathVariable String employeeId) {

        List<Candidate> applications =
                candidateService
                        .getApplicationsByEmployeeId(
                                employeeId
                        );


        return new ResponseEntity<>(
                applications,
                HttpStatus.OK
        );
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Candidate>>
    getCandidatesByJobId(
            @PathVariable Long jobId) {

        List<Candidate> candidates =
                candidateService
                        .getCandidatesByJobId(jobId);


        return new ResponseEntity<>(
                candidates,
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCandidateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {

            String status =
                    body != null
                            ? body.get("status")
                            : null;


            Candidate updated =
                    candidateService
                            .updateCandidateStatus(
                                    id,
                                    status
                            );


            return new ResponseEntity<>(
                    updated,
                    HttpStatus.OK
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/interviews")
    public ResponseEntity<?> scheduleInterview(
            @RequestBody Interview interview) {

        try {

            Interview savedInterview =
                    candidateService
                            .scheduleInterview(
                                    interview
                            );


            return new ResponseEntity<>(
                    savedInterview,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/interviews/candidate/{candidateId}")
    public ResponseEntity<List<Interview>>
    getInterviewsByCandidateId(
            @PathVariable Long candidateId) {

        List<Interview> interviews =
                candidateService
                        .getInterviewsByCandidateId(
                                candidateId
                        );


        return new ResponseEntity<>(
                interviews,
                HttpStatus.OK
        );
    }

    @GetMapping("/notifications/employee/{identifier}")
    public ResponseEntity<List<Notification>>
    getNotificationsForEmployee(
            @PathVariable String identifier) {

        List<Notification> notifications =
                candidateService
                        .getNotificationsForEmployee(
                                identifier
                        );


        return new ResponseEntity<>(
                notifications,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/notifications/employee/{identifier}/unread-count"
    )
    public ResponseEntity<Map<String, Long>>
    getUnreadNotificationCountForEmployee(
            @PathVariable String identifier) {

        long count =
                candidateService
                        .getUnreadNotificationCountForEmployee(
                                identifier
                        );


        Map<String, Long> response =
                new HashMap<>();


        response.put(
                "count",
                count
        );


        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @GetMapping("/notifications/{candidateId}")
    public ResponseEntity<List<Notification>>
    getNotificationsForCandidate(
            @PathVariable Long candidateId) {

        List<Notification> notifications =
                candidateService
                        .getNotificationsForCandidate(
                                candidateId
                        );


        return new ResponseEntity<>(
                notifications,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/notifications/{candidateId}/unread-count"
    )
    public ResponseEntity<Map<String, Long>>
    getUnreadNotificationCount(
            @PathVariable Long candidateId) {

        long count =
                candidateService
                        .getUnreadNotificationCount(
                                candidateId
                        );


        Map<String, Long> response =
                new HashMap<>();


        response.put(
                "count",
                count
        );


        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable Long id) {

        try {

            Notification updated =
                    candidateService
                            .markNotificationAsRead(id);


            return new ResponseEntity<>(
                    updated,
                    HttpStatus.OK
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();


            error.put(
                    "message",
                    e.getMessage()
            );


            return new ResponseEntity<>(
                    error,
                    HttpStatus.NOT_FOUND
            );
        }
    }
}