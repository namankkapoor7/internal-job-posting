package com.example.candidateservice.service;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.entity.Interview;
import com.example.candidateservice.entity.Notification;
import com.example.candidateservice.repository.CandidateRepository;
import com.example.candidateservice.repository.InterviewRepository;
import com.example.candidateservice.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Candidate registerEmployee(Candidate candidate) {

        if (candidate.getFirstName() == null ||
                candidate.getFirstName().trim().isEmpty()) {

            throw new RuntimeException("First Name is required!");
        }

        if (candidate.getLastName() == null ||
                candidate.getLastName().trim().isEmpty()) {

            throw new RuntimeException("Last Name is required!");
        }

        if (candidate.getDob() == null ||
                candidate.getDob().trim().isEmpty()) {

            throw new RuntimeException("Date of Birth is required!");
        }

        if (candidate.getEmployeeId() == null ||
                candidate.getEmployeeId().trim().isEmpty()) {

            throw new RuntimeException("Employee ID is required!");
        }

        if (candidate.getEmail() == null ||
                candidate.getEmail().trim().isEmpty()) {

            throw new RuntimeException("Company Email is required!");
        }

        if (candidate.getPassword() == null ||
                candidate.getPassword().trim().isEmpty()) {

            throw new RuntimeException("Password is required!");
        }


        String email = candidate.getEmail().trim();
        String empId = candidate.getEmployeeId().trim();


        // Company email validation
        if (!email.toLowerCase().endsWith("@company.com")) {

            throw new RuntimeException(
                    "Only company email addresses ending with @company.com are allowed."
            );
        }


        // Duplicate email validation
        List<Candidate> existingByEmail =
                candidateRepository.findByEmailIgnoreCase(email);

        if (!existingByEmail.isEmpty()) {

            throw new RuntimeException(
                    "An employee with this email already exists."
            );
        }


        // Duplicate employee ID validation
        List<Candidate> existingByEmpId =
                candidateRepository.findByEmployeeId(empId);

        if (!existingByEmpId.isEmpty()) {

            throw new RuntimeException(
                    "An employee with this employee ID already exists."
            );
        }

        candidate.setFirstName(candidate.getFirstName().trim());
        candidate.setLastName(candidate.getLastName().trim());
        candidate.setDob(candidate.getDob().trim());
        candidate.setEmployeeId(empId);
        candidate.setEmail(email);
        candidate.setPassword(candidate.getPassword().trim());

        if (candidate.getJobId() == null) {

            candidate.setJobId(0L);
        }

        candidate.setStatus("REGISTERED");


        return candidateRepository.save(candidate);
    }

    public Candidate applyForJob(Candidate candidate) {

        if (candidate.getEmail() == null ||
                candidate.getEmail().trim().isEmpty()) {

            throw new RuntimeException("Email is required!");
        }


        String email = candidate.getEmail().trim();


        // Company email validation
        if (!email.toLowerCase().endsWith("@company.com")) {

            throw new RuntimeException(
                    "Only company email addresses ending with @company.com are allowed."
            );
        }


        if (candidate.getEmployeeId() == null ||
                candidate.getEmployeeId().trim().isEmpty()) {

            throw new RuntimeException("Employee ID is required!");
        }


        String empId = candidate.getEmployeeId().trim();

        Long jobId = candidate.getJobId();

        String password = candidate.getPassword();


        if (password != null) {

            password = password.trim();
        }


        // Find existing employee profile
        List<Candidate> existingByEmail =
                candidateRepository.findByEmailIgnoreCase(email);

        List<Candidate> existingByEmpId =
                candidateRepository.findByEmployeeId(empId);

        if (!existingByEmail.isEmpty() &&
                !existingByEmpId.isEmpty()) {

            String emailForEmpId =
                    existingByEmpId.get(0).getEmail();

            String empIdForEmail =
                    existingByEmail.get(0).getEmployeeId();


            if (!email.equalsIgnoreCase(emailForEmpId) ||
                    !empId.equalsIgnoreCase(empIdForEmail)) {

                throw new RuntimeException(
                        "An employee with this email or employee ID already exists."
                );
            }

        } else if (!existingByEmail.isEmpty()) {

            String existingEmpId =
                    existingByEmail.get(0).getEmployeeId();


            if (!empId.equalsIgnoreCase(existingEmpId)) {

                throw new RuntimeException(
                        "An employee with this email or employee ID already exists."
                );
            }

        } else if (!existingByEmpId.isEmpty()) {

            String existingEmail =
                    existingByEmpId.get(0).getEmail();


            if (!email.equalsIgnoreCase(existingEmail)) {

                throw new RuntimeException(
                        "An employee with this email or employee ID already exists."
                );
            }
        }

        if (!existingByEmail.isEmpty() ||
                !existingByEmpId.isEmpty()) {

            Candidate existingCand =
                    !existingByEmail.isEmpty()
                            ? existingByEmail.get(0)
                            : existingByEmpId.get(0);


            if ((password == null || password.isEmpty()) &&
                    existingCand.getPassword() != null) {

                candidate.setPassword(
                        existingCand.getPassword()
                );

            } else if (password != null &&
                    !password.isEmpty()) {

                candidate.setPassword(password);
            }

        } else {

            // New candidate requires password
            if (password == null || password.isEmpty()) {

                throw new RuntimeException(
                        "Password is required for new candidates!"
                );
            }

            candidate.setPassword(password);
        }

        if (jobId == null || jobId == 0L) {

            /*
             * jobId 0 is not an application.
             */
            if (!existingByEmail.isEmpty() ||
                    !existingByEmpId.isEmpty()) {

                throw new RuntimeException(
                        "An employee with this email or employee ID already exists."
                );
            }

        } else {

            Optional<Candidate> existingAppByEmail =
                    candidateRepository
                            .findByEmailIgnoreCaseAndJobId(
                                    email,
                                    jobId
                            );


            if (existingAppByEmail.isPresent()) {

                throw new RuntimeException(
                        "You have already applied for this job."
                );
            }


            Optional<Candidate> existingAppByEmp =
                    candidateRepository
                            .findByEmployeeIdAndJobId(
                                    empId,
                                    jobId
                            );


            if (existingAppByEmp.isPresent()) {

                throw new RuntimeException(
                        "You have already applied for this job."
                );
            }

            JobPostingDto job = null;

            try {

                job = jobServiceClient.getJobById(jobId);

            } catch (Exception e) {

                throw new RuntimeException(
                        "Job with ID " + jobId + " does not exist!"
                );
            }


            if (job == null) {

                throw new RuntimeException(
                        "Job with ID " + jobId + " does not exist!"
                );
            }

            if (!"OPEN".equalsIgnoreCase(job.getStatus())) {

                throw new RuntimeException(
                        "Cannot apply: Job with ID " +
                                jobId +
                                " is CLOSED!"
                );
            }
        }


        /*
         * Only actual job applications should get APPLIED.
         */
        if (candidate.getStatus() == null ||
                candidate.getStatus().trim().isEmpty()) {

            candidate.setStatus("APPLIED");
        }


        candidate.setEmail(email);
        candidate.setEmployeeId(empId);


        // Save application
        return candidateRepository.save(candidate);
    }

    public List<Candidate> getAllCandidates() {

        return candidateRepository.findAll();
    }

    public Optional<Candidate> getCandidateById(Long id) {

        return candidateRepository.findById(id);
    }

    public List<Candidate> getCandidatesByJobId(Long jobId) {

        return candidateRepository.findByJobId(jobId);
    }

    public List<Candidate> getCandidatesByEmail(String email) {

        return candidateRepository
                .findByEmailIgnoreCase(email.trim());
    }

    public List<Candidate> getCandidatesByEmployeeId(
            String employeeId) {

        return candidateRepository
                .findByEmployeeId(employeeId.trim());
    }

    public List<Candidate> getApplicationsByEmployeeId(
            String employeeId) {

        return candidateRepository
                .findByEmployeeIdAndJobIdGreaterThan(
                        employeeId.trim(),
                        0L
                );
    }

    @Transactional
    public void deleteCandidate(Long id) {

        Optional<Candidate> optionalCandidate =
                candidateRepository.findById(id);


        if (optionalCandidate.isPresent()) {

            notificationRepository.deleteByCandidateId(id);

            interviewRepository.deleteByCandidateId(id);

            candidateRepository.deleteById(id);

        } else {

            throw new RuntimeException(
                    "Candidate record with ID " +
                            id +
                            " not found!"
            );
        }
    }

    public Candidate updateCandidateStatus(
            Long candidateId,
            String status) {

        Optional<Candidate> optionalCandidate =
                candidateRepository.findById(candidateId);


        if (optionalCandidate.isPresent()) {

            Candidate candidate =
                    optionalCandidate.get();

            candidate.setStatus(status);


            Candidate saved =
                    candidateRepository.save(candidate);


            createStatusNotification(
                    saved,
                    status
            );


            return saved;
        }


        throw new RuntimeException(
                "Candidate with ID " +
                        candidateId +
                        " not found!"
        );
    }

    private void createStatusNotification(
            Candidate candidate,
            String status) {

        String jobTitle =
                "Job #" + candidate.getJobId();


        try {

            JobPostingDto job =
                    jobServiceClient.getJobById(
                            candidate.getJobId()
                    );


            if (job != null &&
                    job.getDesignation() != null) {

                jobTitle =
                        job.getDesignation();
            }

        } catch (Exception e) {

            // fallback
        }


        String title = "";
        String message = "";
        String type = status;


        if ("SHORTLISTED".equalsIgnoreCase(status)) {

            title = "Profile Shortlisted";

            message =
                    "Your profile has been shortlisted for " +
                            jobTitle +
                            ".";

        } else if ("SELECTED".equalsIgnoreCase(status)) {

            title = "Application Selected";

            message =
                    "Congratulations! You have been selected for the position of " +
                            jobTitle +
                            ".";

        } else if ("REJECTED".equalsIgnoreCase(status)) {

            title = "Application Update";

            message =
                    "Thank you for your interest. Your application for " +
                            jobTitle +
                            " was not selected at this time.";

        } else {

            return;
        }


        String now =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                )
                        );


        Notification notification =
                new Notification(
                        null,
                        candidate.getId(),
                        title,
                        message,
                        type,
                        false,
                        now
                );


        notificationRepository.save(
                notification
        );
    }

    public Interview scheduleInterview(
            Interview interview) {

        if (interview.getCandidateId() == null) {

            throw new RuntimeException(
                    "Candidate ID must be provided!"
            );
        }


        Optional<Candidate> optionalCandidate =
                candidateRepository.findById(
                        interview.getCandidateId()
                );


        if (optionalCandidate.isEmpty()) {

            throw new RuntimeException(
                    "Candidate with ID " +
                            interview.getCandidateId() +
                            " not found!"
            );
        }


        Candidate candidate =
                optionalCandidate.get();


        if (interview.getJobId() == null) {

            interview.setJobId(
                    candidate.getJobId()
            );
        }


        String mode =
                interview.getInterviewMode();


        if (mode == null ||
                mode.trim().isEmpty()) {

            mode = "OFFLINE";

            interview.setInterviewMode(mode);
        }

        if ("ONLINE".equalsIgnoreCase(mode)) {

            if (interview.getMeetingLink() == null ||
                    interview.getMeetingLink()
                            .trim()
                            .isEmpty()) {

                throw new RuntimeException(
                        "Meeting Link is required for ONLINE interviews!"
                );
            }


            interview.setLocation(null);

        }

        else if ("OFFLINE".equalsIgnoreCase(mode)) {

            if (interview.getLocation() == null ||
                    interview.getLocation()
                            .trim()
                            .isEmpty()) {

                throw new RuntimeException(
                        "Meeting Room Name / Location is required for OFFLINE interviews!"
                );
            }


            interview.setMeetingLink(null);
        }


        interview.setStatus("SCHEDULED");


        Interview savedInterview =
                interviewRepository.save(
                        interview
                );


        candidate.setStatus(
                "INTERVIEW_SCHEDULED"
        );


        candidateRepository.save(
                candidate
        );

        String jobTitle =
                "Job #" + candidate.getJobId();


        try {

            JobPostingDto job =
                    jobServiceClient.getJobById(
                            candidate.getJobId()
                    );


            if (job != null &&
                    job.getDesignation() != null) {

                jobTitle =
                        job.getDesignation();
            }

        } catch (Exception e) {

            // fallback
        }


        String title =
                "Interview Scheduled";


        StringBuilder sb =
                new StringBuilder();


        sb.append(
                        "Your interview for "
                )
                .append(jobTitle)
                .append(" is scheduled on ")
                .append(interview.getInterviewDate());


        if (interview.getInterviewTime() != null &&
                !interview.getInterviewTime()
                        .trim()
                        .isEmpty()) {

            sb.append(" at ")
                    .append(interview.getInterviewTime());
        }


        if (interview.getInterviewer() != null &&
                !interview.getInterviewer()
                        .trim()
                        .isEmpty()) {

            sb.append(". Interviewer: ")
                    .append(interview.getInterviewer());
        }


        if ("ONLINE".equalsIgnoreCase(mode)) {

            sb.append(
                            ". Mode: ONLINE. Meeting Link: "
                    )
                    .append(interview.getMeetingLink())
                    .append(".");

        } else {

            sb.append(
                            ". Mode: OFFLINE. Room Location: "
                    )
                    .append(interview.getLocation())
                    .append(".");
        }


        String now =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                )
                        );


        Notification notification =
                new Notification(
                        null,
                        candidate.getId(),
                        title,
                        sb.toString(),
                        "INTERVIEW_SCHEDULED",
                        false,
                        now
                );


        notificationRepository.save(
                notification
        );


        return savedInterview;
    }

    public Candidate loginEmployee(
            String email,
            String password) {

        if (email == null ||
                email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Company Email is required!"
            );
        }


        if (password == null ||
                password.trim().isEmpty()) {

            throw new RuntimeException(
                    "Invalid email or password."
            );
        }


        String emailTrimmed =
                email.trim();

        String passTrimmed =
                password.trim();


        // Company email validation
        if (!emailTrimmed.contains("@") ||
                !emailTrimmed
                        .toLowerCase()
                        .endsWith("@company.com")) {

            throw new RuntimeException(
                    "Only company email addresses ending with @company.com are allowed."
            );
        }


        // Login using email
        List<Candidate> list =
                candidateRepository
                        .findByEmailIgnoreCase(
                                emailTrimmed
                        );


        if (list.isEmpty()) {

            throw new RuntimeException(
                    "Invalid email or password."
            );
        }


        for (Candidate cand : list) {

            if (cand.getPassword() == null ||
                    cand.getPassword()
                            .trim()
                            .isEmpty()) {

                cand.setPassword(
                        passTrimmed
                );

                candidateRepository.save(
                        cand
                );

                return cand;

            } else if (cand.getPassword()
                    .trim()
                    .equals(passTrimmed)) {

                return cand;
            }
        }


        throw new RuntimeException(
                "Invalid email or password."
        );
    }

    public List<Interview> getInterviewsByCandidateId(
            Long candidateId) {

        return interviewRepository
                .findByCandidateId(candidateId);
    }

    public List<Notification> getNotificationsForEmployee(
            String identifier) {

        if (identifier == null ||
                identifier.trim().isEmpty()) {

            return Collections.emptyList();
        }


        String query =
                identifier.trim();


        List<Candidate> candidates =
                candidateRepository
                        .findByEmailIgnoreCase(query);


        if (candidates.isEmpty()) {

            candidates =
                    candidateRepository
                            .findByEmployeeId(query);
        }


        if (candidates.isEmpty()) {

            try {

                Long cId =
                        Long.parseLong(query);


                Optional<Candidate> cOpt =
                        candidateRepository
                                .findById(cId);


                if (cOpt.isPresent()) {

                    candidates =
                            candidateRepository
                                    .findByEmailIgnoreCase(
                                            cOpt.get().getEmail()
                                    );


                    if (candidates.isEmpty()) {

                        candidates =
                                candidateRepository
                                        .findByEmployeeId(
                                                cOpt.get()
                                                        .getEmployeeId()
                                        );
                    }
                }

            } catch (NumberFormatException e) {

                // ignore
            }
        }


        if (candidates.isEmpty()) {

            return Collections.emptyList();
        }


        List<Long> candidateIds =
                candidates.stream()
                        .map(Candidate::getId)
                        .collect(Collectors.toList());


        return notificationRepository
                .findByCandidateIdInOrderByIdDesc(
                        candidateIds
                );
    }

    public long getUnreadNotificationCountForEmployee(
            String identifier) {

        if (identifier == null ||
                identifier.trim().isEmpty()) {

            return 0L;
        }


        String query =
                identifier.trim();


        List<Candidate> candidates =
                candidateRepository
                        .findByEmailIgnoreCase(query);


        if (candidates.isEmpty()) {

            candidates =
                    candidateRepository
                            .findByEmployeeId(query);
        }


        if (candidates.isEmpty()) {

            try {

                Long cId =
                        Long.parseLong(query);


                Optional<Candidate> cOpt =
                        candidateRepository
                                .findById(cId);


                if (cOpt.isPresent()) {

                    candidates =
                            candidateRepository
                                    .findByEmailIgnoreCase(
                                            cOpt.get().getEmail()
                                    );


                    if (candidates.isEmpty()) {

                        candidates =
                                candidateRepository
                                        .findByEmployeeId(
                                                cOpt.get()
                                                        .getEmployeeId()
                                        );
                    }
                }

            } catch (NumberFormatException e) {

                // ignore
            }
        }


        if (candidates.isEmpty()) {

            return 0L;
        }


        List<Long> candidateIds =
                candidates.stream()
                        .map(Candidate::getId)
                        .collect(Collectors.toList());


        return notificationRepository
                .countByCandidateIdInAndIsReadFalse(
                        candidateIds
                );
    }

    public List<Notification> getNotificationsForCandidate(
            Long candidateId) {

        return getNotificationsForEmployee(
                candidateId.toString()
        );
    }


    // =========================================================
    // GET UNREAD COUNT BY CANDIDATE ID
    // =========================================================

    public long getUnreadNotificationCount(
            Long candidateId) {

        return getUnreadNotificationCountForEmployee(
                candidateId.toString()
        );
    }


    // =========================================================
    // MARK NOTIFICATION AS READ
    // =========================================================

    public Notification markNotificationAsRead(
            Long notificationId) {

        Optional<Notification> optional =
                notificationRepository
                        .findById(notificationId);


        if (optional.isPresent()) {

            Notification notification =
                    optional.get();


            notification.setRead(true);


            return notificationRepository
                    .save(notification);
        }


        throw new RuntimeException(
                "Notification not found!"
        );
    }
}