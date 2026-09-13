package com.example.jobservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobId;
    private String title;
    
    @Column(length = 4000)
    private String description;
    private String designation;
    private String department;
    private String location;
    private String skillSet;
    private String experience;
    private Double salaryMin;
    private Double salaryMax;
    private String status; // DRAFT, PUBLISHED, PAUSED, CLOSED, EXPIRED
    private LocalDateTime postedAt;
    private String closingDate;

    public JobPosting() {
        this.status = "OPEN";
        this.postedAt = LocalDateTime.now();
    }

    public JobPosting(Long id, String jobId, String description, String designation, String location, 
                      String skillSet, String experience, Double salaryMin, Double salaryMax, String status) {
        this.id = id;
        this.jobId = jobId;
        this.description = description;
        this.designation = designation;
        this.location = location;
        this.skillSet = skillSet;
        this.experience = experience;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.status = status != null ? status : "OPEN";
        this.postedAt = LocalDateTime.now();
    }

    public JobPosting(Long id, String jobId, String title, String description, String designation, String department,
                      String location, String skillSet, String experience, Double salaryMin, Double salaryMax, String status) {
        this.id = id;
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.designation = designation;
        this.department = department;
        this.location = location;
        this.skillSet = skillSet;
        this.experience = experience;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.status = status != null ? status : "OPEN";
        this.postedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title != null ? title : designation;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(String skillSet) {
        this.skillSet = skillSet;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Double getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Double salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Double getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Double salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public String getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(String closingDate) {
        this.closingDate = closingDate;
    }
}
