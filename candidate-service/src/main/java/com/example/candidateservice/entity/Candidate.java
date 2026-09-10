package com.example.candidateservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "candidate", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "jobId"}),
    @UniqueConstraint(columnNames = {"employeeId", "jobId"})
})
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String employeeId;
    private String dob;
    private String email;
    private String password;
    private Long jobId; // Stored as plain Long ID (No JPA entity relationship across microservices)
    private String status; // "APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED"

    public Candidate() {
        this.status = "APPLIED";
    }

    public Candidate(Long id, String firstName, String lastName, String employeeId, String dob, String email, Long jobId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.dob = dob;
        this.email = email;
        this.jobId = jobId;
        this.status = "APPLIED";
    }

    public Candidate(Long id, String firstName, String lastName, String employeeId, String dob, String email, String password, Long jobId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.dob = dob;
        this.email = email;
        this.password = password;
        this.jobId = jobId;
        this.status = "APPLIED";
    }

    public Candidate(Long id, String firstName, String lastName, String employeeId, String dob, String email, Long jobId, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.dob = dob;
        this.email = email;
        this.jobId = jobId;
        this.status = status != null ? status : "APPLIED";
    }

    public Candidate(Long id, String firstName, String lastName, String employeeId, String dob, String email, String password, Long jobId, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.dob = dob;
        this.email = email;
        this.password = password;
        this.jobId = jobId;
        this.status = status != null ? status : "APPLIED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
