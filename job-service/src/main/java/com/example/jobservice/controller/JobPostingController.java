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
    public ResponseEntity<List<JobPosting>> getJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skill) {

        if (status == null && designation == null && location == null && skill == null) {
            return new ResponseEntity<>(jobPostingService.getAllJobs(), HttpStatus.OK);
        }
        List<JobPosting> jobs = jobPostingService.searchJobs(status, designation, location, skill);
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
        return job.map(jobPosting -> new ResponseEntity<>(jobPosting, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody JobPosting updatedJob) {
        try {
            JobPosting result = jobPostingService.updateJob(id, updatedJob);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body != null ? body.get("status") : null;
            JobPosting result = jobPostingService.updateJobStatus(id, status);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeJob(@PathVariable Long id) {
        try {
            JobPosting result = jobPostingService.closeJob(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }
}
