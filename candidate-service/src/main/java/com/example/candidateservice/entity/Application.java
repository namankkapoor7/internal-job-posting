package com.example.candidateservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application", indexes = {
    @Index(name = "idx_app_emp", columnList = "employeeId"),
    @Index(name = "idx_app_job", columnList = "jobId")
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private Long jobId;

    private String jobSnapshotTitle;
    private String jobSnapshotDesignation;
    private String jobSnapshotLocation;
    private String jobSnapshotDepartment;

    @Column(length = 2000)
    private String coverNote;

    private Long documentId;

    @Column(nullable = false)
    private String status; // SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED, WITHDRAWN

    @Column(length = 2000)
    private String reviewerNotes; // Private HR notes

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public Application() {
        this.status = "SUBMITTED";
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Application(Long id, String employeeId, Long jobId, String jobSnapshotTitle, 
                       String jobSnapshotDesignation, String jobSnapshotLocation, String jobSnapshotDepartment, 
                       String coverNote, Long documentId) {
        this.id = id;
        this.employeeId = employeeId;
        this.jobId = jobId;
        this.jobSnapshotTitle = jobSnapshotTitle;
        this.jobSnapshotDesignation = jobSnapshotDesignation;
        this.jobSnapshotLocation = jobSnapshotLocation;
        this.jobSnapshotDepartment = jobSnapshotDepartment;
        this.coverNote = coverNote;
        this.documentId = documentId;
        this.status = "SUBMITTED";
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getJobSnapshotTitle() {
        return jobSnapshotTitle;
    }

    public void setJobSnapshotTitle(String jobSnapshotTitle) {
        this.jobSnapshotTitle = jobSnapshotTitle;
    }

    public String getJobSnapshotDesignation() {
        return jobSnapshotDesignation;
    }

    public void setJobSnapshotDesignation(String jobSnapshotDesignation) {
        this.jobSnapshotDesignation = jobSnapshotDesignation;
    }

    public String getJobSnapshotLocation() {
        return jobSnapshotLocation;
    }

    public void setJobSnapshotLocation(String jobSnapshotLocation) {
        this.jobSnapshotLocation = jobSnapshotLocation;
    }

    public String getJobSnapshotDepartment() {
        return jobSnapshotDepartment;
    }

    public void setJobSnapshotDepartment(String jobSnapshotDepartment) {
        this.jobSnapshotDepartment = jobSnapshotDepartment;
    }

    public String getCoverNote() {
        return coverNote;
    }

    public void setCoverNote(String coverNote) {
        this.coverNote = coverNote;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
