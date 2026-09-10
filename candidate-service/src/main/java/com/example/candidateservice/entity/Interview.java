package com.example.candidateservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interview")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId; // Maps to candidateId / applicationId
    private Long candidateId;
    private Long jobId;
    private String interviewMode; // "ONLINE", "OFFLINE"
    private String interviewDate;
    private String interviewTime;
    private String location; // Meeting room name for OFFLINE
    private String meetingLink; // Meeting link for ONLINE
    private String interviewer;
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED"

    public Interview() {
        this.status = "SCHEDULED";
    }

    public Interview(Long id, Long applicationId, Long candidateId, Long jobId, String interviewMode,
                     String interviewDate, String interviewTime, String location, String meetingLink, 
                     String interviewer, String status) {
        this.id = id;
        this.applicationId = applicationId != null ? applicationId : candidateId;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.interviewMode = interviewMode != null ? interviewMode : "OFFLINE";
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.location = location;
        this.meetingLink = meetingLink;
        this.interviewer = interviewer;
        this.status = status != null ? status : "SCHEDULED";
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

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
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
}
