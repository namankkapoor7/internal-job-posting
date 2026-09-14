package com.example.jobservice.service;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================
    // POSITIVE TEST CASES
    // =========================================================

    @Test
    public void testCreateJob_Success() {
        JobPosting job = new JobPosting(
            null, "JOB101", "Developer Role", "Job Description", "Java Developer",
            "Engineering", "Bangalore", "Java, Spring Boot", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(job);

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
        assertEquals("Java Developer", created.getDesignation());
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testCreateJob_DefaultStatus() {
        JobPosting job = new JobPosting(
            null, "JOB102", "Backend Role", "Job Description", "Backend Developer",
            "Infrastructure", "Noida", "Java", "2 years",
            60000.0, 90000.0, null
        );

        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testCloseJob_Success() {
        JobPosting job = new JobPosting(
            1L, "JOB101", "Developer Role", "Job Description", "Java Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting closed = jobPostingService.closeJob(1L);

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getStatus());
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testDeleteJob_Success() {
        JobPosting job = new JobPosting(
            1L, "JOB101", "Developer Role", "Job Description", "Java Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));

        jobPostingService.deleteJob(1L);

        verify(jobPostingRepository, times(1)).deleteById(1L);
    }

    // =========================================================
    // NEGATIVE TEST CASES (Null, Blank, Boundary & Side Effects)
    // =========================================================

    @Test
    public void testCreateJob_NullInput() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(null)
        );

        assertEquals("Job posting object cannot be null!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_MissingDesignation() {
        JobPosting job = new JobPosting(
            null, "JOB103", "Title", "Description", "",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Job designation is required!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_NegativeSalary() {
        JobPosting job = new JobPosting(
            null, "JOB104", "Title", "Description", "Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            -500.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Minimum salary cannot be negative!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_InvertedSalaryRange() {
        JobPosting job = new JobPosting(
            null, "JOB105", "Title", "Description", "Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            90000.0, 50000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Maximum salary cannot be less than minimum salary!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testDeleteJob_NotFound() {
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.deleteJob(99L)
        );

        assertEquals("Job posting with ID 99 not found!", ex.getMessage());
        verify(jobPostingRepository, never()).deleteById(any());
    }

    @Test
    public void testUpdateJob_NotFound() {
        JobPosting updated = new JobPosting(
            null, "JOB99", "T", "D", "Desig", "Dept", "Loc", "Skill", "1 yr", 10.0, 20.0, "OPEN"
        );

        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.updateJob(99L, updated)
        );

        assertEquals("Job posting with ID 99 not found!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testUpdateJob_NullId() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.updateJob(null, new JobPosting())
        );

        assertEquals("Job ID cannot be null!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_MissingTitle() {
        JobPosting job = new JobPosting(
            null, "JOB106", "", "Description", "Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Job title is required!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_MissingLocation() {
        JobPosting job = new JobPosting(
            null, "JOB107", "Title", "Description", "Developer",
            "Engineering", "   ", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Job location is required!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_MissingDescription() {
        JobPosting job = new JobPosting(
            null, "JOB108", "Title", "", "Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Job description is required!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testCreateJob_MissingExperience() {
        JobPosting job = new JobPosting(
            null, "JOB109", "Title", "Description", "Developer",
            "Engineering", "Bangalore", "Java", "",
            50000.0, 80000.0, "OPEN"
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.createJob(job)
        );

        assertEquals("Job experience requirement is required!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testUpdateJob_NegativeSalary() {
        JobPosting existingJob = new JobPosting(
            1L, "JOB101", "Developer Role", "Job Description", "Java Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        JobPosting update = new JobPosting();
        update.setSalaryMin(-1000.0);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existingJob));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.updateJob(1L, update)
        );

        assertEquals("Minimum salary cannot be negative!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    public void testUpdateJob_InvertedSalaryRange() {
        JobPosting existingJob = new JobPosting(
            1L, "JOB101", "Developer Role", "Job Description", "Java Developer",
            "Engineering", "Bangalore", "Java", "2 years",
            50000.0, 80000.0, "OPEN"
        );

        JobPosting update = new JobPosting();
        update.setSalaryMin(100000.0);
        update.setSalaryMax(40000.0);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existingJob));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> jobPostingService.updateJob(1L, update)
        );

        assertEquals("Maximum salary cannot be less than minimum salary!", ex.getMessage());
        verify(jobPostingRepository, never()).save(any());
    }
}