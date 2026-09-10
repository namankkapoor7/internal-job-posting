package com.example.jobservice.service;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import com.example.jobservice.service.JobPostingService;
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

    // =========================
    // POSITIVE TESTS
    // =========================

    @Test
    public void testCreateJob() {

        JobPosting job = new JobPosting(
                null,
                "JOB101",
                "Developer Role",
                "Java Developer",
                "Bangalore",
                "Java, Spring Boot",
                "2 years",
                50000.0,
                80000.0,
                "OPEN"
        );

        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenReturn(job);

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
        assertEquals("Java Developer", created.getDesignation());

        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testCreateJob_DefaultStatus() {

        JobPosting job = new JobPosting(
                null,
                "JOB102",
                "Backend Role",
                "Backend Developer",
                "Noida",
                "Java, Spring Boot",
                "2 years",
                60000.0,
                90000.0,
                null
        );

        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());

        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testCloseJob() {

        JobPosting job = new JobPosting(
                1L,
                "JOB101",
                "Developer Role",
                "Java Developer",
                "Bangalore",
                "Java, Spring Boot",
                "2 years",
                50000.0,
                80000.0,
                "OPEN"
        );

        when(jobPostingRepository.findById(1L))
                .thenReturn(Optional.of(job));

        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting closed = jobPostingService.closeJob(1L);

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getStatus());

        verify(jobPostingRepository, times(1)).findById(1L);
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void testDeleteJob_Success() {

        JobPosting job = new JobPosting(
                1L,
                "JOB101",
                "Developer Role",
                "Java Developer",
                "Bangalore",
                "Java, Spring Boot",
                "2 years",
                50000.0,
                80000.0,
                "OPEN"
        );

        when(jobPostingRepository.findById(1L))
                .thenReturn(Optional.of(job));

        jobPostingService.deleteJob(1L);

        verify(jobPostingRepository, times(1))
                .deleteById(1L);
    }


    // NEGATIVE TESTS

    @Test
    public void testDeleteJob_NotFound() {

        when(jobPostingRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> jobPostingService.deleteJob(99L)
        );

        assertTrue(
                exception.getMessage().contains("not found")
        );

        verify(jobPostingRepository, times(1))
                .findById(99L);

        verify(jobPostingRepository, never())
                .deleteById(99L);
    }

    @Test
    public void testUpdateJob_NotFound() {

        JobPosting updatedJob = new JobPosting(
                null,
                "JOB999",
                "Updated Role",
                "Senior Developer",
                "Delhi",
                "Java",
                "5 years",
                80000.0,
                120000.0,
                "OPEN"
        );

        when(jobPostingRepository.findById(999L))
                .thenReturn(Optional.empty());

        JobPosting result =
                jobPostingService.updateJob(999L, updatedJob);

        assertNull(result);

        verify(jobPostingRepository, times(1))
                .findById(999L);

        verify(jobPostingRepository, never())
                .save(any(JobPosting.class));
    }

    @Test
    public void testCloseJob_NotFound() {

        when(jobPostingRepository.findById(999L))
                .thenReturn(Optional.empty());

        JobPosting result =
                jobPostingService.closeJob(999L);

        assertNull(result);

        verify(jobPostingRepository, times(1))
                .findById(999L);

        verify(jobPostingRepository, never())
                .save(any(JobPosting.class));
    }
}