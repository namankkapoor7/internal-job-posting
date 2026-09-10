package com.example.jobservice.controller;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    @PostMapping
    public ResponseEntity<JobPosting> createJob(@RequestBody JobPosting jobPosting) {
        JobPosting createdJob = jobPostingService.createJob(jobPosting);
        return new ResponseEntity<>(createdJob, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<JobPosting>> getAllJobs() {
        List<JobPosting> jobs = jobPostingService.getAllJobs();
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping("/open")
    public ResponseEntity<List<JobPosting>> getOpenJobs() {
        List<JobPosting> openJobs = jobPostingService.getOpenJobs();
        return new ResponseEntity<>(openJobs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPosting> getJobById(@PathVariable Long id) {
        Optional<JobPosting> job = jobPostingService.getJobById(id);
        if (job.isPresent()) {
            return new ResponseEntity<>(job.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            jobPostingService.deleteJob(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Job posting deleted successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPosting> updateJob(@PathVariable Long id, @RequestBody JobPosting updatedJob) {
        JobPosting result = jobPostingService.updateJob(id, updatedJob);
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<JobPosting> closeJob(@PathVariable Long id) {
        JobPosting result = jobPostingService.closeJob(id);
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
