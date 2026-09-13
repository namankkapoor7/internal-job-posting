package com.example.candidateservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview", indexes = {
    @Index(name = "idx_int_app", columnList = "applicationId"),
    @Index(name = "idx_int_emp", columnList = "employeeId")
})
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    private String employeeId;
    private Long jobId;

    private String interviewMode; // "ONLINE", "OFFLINE"
    private String interviewDate;
    private String interviewTime;
    private String location; // Meeting room name for OFFLINE
    private String meetingLink; // Meeting link for ONLINE
    private String interviewer;
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED"
    
    @Column(length = 2000)
    private String feedbackNotes;

    private LocalDateTime createdAt;

    public Interview() {
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }

    public Interview(Long id, Long applicationId, String employeeId, Long jobId, String interviewMode,
                     String interviewDate, String interviewTime, String location, String meetingLink, 
                     String interviewer, String status) {
        this.id = id;
        this.applicationId = applicationId;
        this.employeeId = employeeId;
        this.jobId = jobId;
        this.interviewMode = interviewMode != null ? interviewMode : "OFFLINE";
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.location = location;
        this.meetingLink = meetingLink;
        this.interviewer = interviewer;
        this.status = status != null ? status : "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedbackNotes() {
        return feedbackNotes;
    }

    public void setFeedbackNotes(String feedbackNotes) {
        this.feedbackNotes = feedbackNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
