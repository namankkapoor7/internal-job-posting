package com.example.jobservice.service;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @PostConstruct
    public void initSampleJobs() {
        if (jobPostingRepository.count() == 0) {
            JobPosting job1 = new JobPosting(
                null, "JOB-101", "Senior Full Stack Engineer",
                "Looking for experienced Java and Angular developer to lead internal microservice transformation.",
                "Java Full Stack Developer", "Engineering", "Bangalore", "Java 17, Spring Boot, Angular, Microservices",
                "4-6 years", 1200000.0, 1800000.0, "PUBLISHED"
            );
            JobPosting job2 = new JobPosting(
                null, "JOB-102", "Lead Backend Architect",
                "Architect resilient cloud native services with high throughput data ingestion pipelines.",
                "Backend Engineer", "Infrastructure", "Remote", "Java, Spring Cloud, Docker, Kubernetes, MySQL",
                "6-8 years", 1800000.0, 2600000.0, "PUBLISHED"
            );
            JobPosting job3 = new JobPosting(
                null, "JOB-103", "UI/UX Front End Specialist",
                "Design accessible and clean internal enterprise web interfaces.",
                "Angular Developer", "UX Design", "Noida", "TypeScript, Angular 17, HTML5, CSS3, Accessibility",
                "3-5 years", 1000000.0, 1500000.0, "PUBLISHED"
            );
            jobPostingRepository.saveAll(List.of(job1, job2, job3));
        }
    }

    private void validateJobPostingInput(JobPosting jobPosting) {
        if (jobPosting == null) {
            throw new RuntimeException("Job posting object cannot be null!");
        }
        if (jobPosting.getTitle() == null || jobPosting.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Job title is required!");
        }
        if (jobPosting.getDesignation() == null || jobPosting.getDesignation().trim().isEmpty()) {
            throw new RuntimeException("Job designation is required!");
        }
        if (jobPosting.getLocation() == null || jobPosting.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Job location is required!");
        }
        if (jobPosting.getDescription() == null || jobPosting.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Job description is required!");
        }
        if (jobPosting.getExperience() == null || jobPosting.getExperience().trim().isEmpty()) {
            throw new RuntimeException("Job experience requirement is required!");
        }
        if (jobPosting.getSalaryMin() != null && jobPosting.getSalaryMin() < 0) {
            throw new RuntimeException("Minimum salary cannot be negative!");
        }
        if (jobPosting.getSalaryMin() != null && jobPosting.getSalaryMax() != null && jobPosting.getSalaryMax() < jobPosting.getSalaryMin()) {
            throw new RuntimeException("Maximum salary cannot be less than minimum salary!");
        }
    }

    public JobPosting createJob(JobPosting jobPosting) {
        validateJobPostingInput(jobPosting);
        if (jobPosting.getStatus() == null || jobPosting.getStatus().trim().isEmpty()) {
            jobPosting.setStatus("OPEN");
        }
        if (jobPosting.getJobId() == null || jobPosting.getJobId().trim().isEmpty()) {
            jobPosting.setJobId("JOB-" + (System.currentTimeMillis() % 10000));
        }
        return jobPostingRepository.save(jobPosting);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    public List<JobPosting> searchJobs(String status, String designation, String location, String skill) {
        String queryStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        return jobPostingRepository.filterJobs(queryStatus, designation, location, skill);
    }

    public List<JobPosting> getOpenJobs() {
        List<JobPosting> published = jobPostingRepository.findByStatus("PUBLISHED");
        if (published.isEmpty()) {
            return jobPostingRepository.findByStatus("OPEN");
        }
        return published;
    }

    public Optional<JobPosting> getJobById(Long id) {
        return jobPostingRepository.findById(id);
    }

    public void deleteJob(Long id) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            jobPostingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Job posting with ID " + id + " not found!");
        }
    }

    public JobPosting updateJob(Long id, JobPosting updatedJob) {
        if (id == null) {
            throw new RuntimeException("Job ID cannot be null!");
        }
        if (updatedJob == null) {
            throw new RuntimeException("Updated job details cannot be null!");
        }
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            if (updatedJob.getTitle() != null) existingJob.setTitle(updatedJob.getTitle());
            if (updatedJob.getDescription() != null) existingJob.setDescription(updatedJob.getDescription());
            if (updatedJob.getDesignation() != null) existingJob.setDesignation(updatedJob.getDesignation());
            if (updatedJob.getDepartment() != null) existingJob.setDepartment(updatedJob.getDepartment());
            if (updatedJob.getLocation() != null) existingJob.setLocation(updatedJob.getLocation());
            if (updatedJob.getSkillSet() != null) existingJob.setSkillSet(updatedJob.getSkillSet());
            if (updatedJob.getExperience() != null) existingJob.setExperience(updatedJob.getExperience());
            if (updatedJob.getSalaryMin() != null) existingJob.setSalaryMin(updatedJob.getSalaryMin());
            if (updatedJob.getSalaryMax() != null) existingJob.setSalaryMax(updatedJob.getSalaryMax());
            if (updatedJob.getStatus() != null) existingJob.setStatus(updatedJob.getStatus());
            validateJobPostingInput(existingJob);
            return jobPostingRepository.save(existingJob);
        }
        throw new RuntimeException("Job posting with ID " + id + " not found!");
    }

    public JobPosting updateJobStatus(Long id, String status) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            existingJob.setStatus(status != null ? status.toUpperCase() : "CLOSED");
            return jobPostingRepository.save(existingJob);
        }
        return null;
    }

    public JobPosting closeJob(Long id) {
        return updateJobStatus(id, "CLOSED");
    }
}
