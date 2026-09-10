package com.example.jobservice.service;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public JobPosting createJob(JobPosting jobPosting) {
        if (jobPosting.getStatus() == null || jobPosting.getStatus().trim().isEmpty()) {
            jobPosting.setStatus("OPEN");
        }
        return jobPostingRepository.save(jobPosting);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    public List<JobPosting> getOpenJobs() {
        return jobPostingRepository.findByStatus("OPEN");
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
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            existingJob.setDescription(updatedJob.getDescription());
            existingJob.setDesignation(updatedJob.getDesignation());
            existingJob.setLocation(updatedJob.getLocation());
            existingJob.setSkillSet(updatedJob.getSkillSet());
            existingJob.setExperience(updatedJob.getExperience());
            existingJob.setSalaryMin(updatedJob.getSalaryMin());
            existingJob.setSalaryMax(updatedJob.getSalaryMax());
            if (updatedJob.getStatus() != null) {
                existingJob.setStatus(updatedJob.getStatus());
            }
            return jobPostingRepository.save(existingJob);
        }
        return null;
    }

    public JobPosting closeJob(Long id) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            existingJob.setStatus("CLOSED");
            return jobPostingRepository.save(existingJob);
        }
        return null;
    }
}
