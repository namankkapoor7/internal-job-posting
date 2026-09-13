package com.example.candidateservice.dto;

public class JobPostingDto {

    private Long id;
    private String jobId;
    private String title;
    private String description;
    private String designation;
    private String department;
    private String location;
    private String skillSet;
    private String experience;
    private Double salaryMin;
    private Double salaryMax;
    private String status;

    public JobPostingDto() {
    }

    public JobPostingDto(Long id, String jobId, String designation, String status) {
        this.id = id;
        this.jobId = jobId;
        this.designation = designation;
        this.title = designation;
        this.status = status;
    }

    public JobPostingDto(Long id, String jobId, String title, String designation, String department, String location, String status) {
        this.id = id;
        this.jobId = jobId;
        this.title = title;
        this.designation = designation;
        this.department = department;
        this.location = location;
        this.status = status;
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
}
