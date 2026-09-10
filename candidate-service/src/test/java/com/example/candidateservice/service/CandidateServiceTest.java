package com.example.candidateservice.service;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.repository.CandidateRepository;
import com.example.candidateservice.repository.InterviewRepository;
import com.example.candidateservice.repository.NotificationRepository;
import com.example.candidateservice.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @InjectMocks
    private CandidateService candidateService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // POSITIVE TESTS
    // =========================

    @Test
    public void testApplyForJob_Success() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@company.com",
                "Test@123",
                1L
        );

        JobPostingDto openJob =
                new JobPostingDto(
                        1L,
                        "JOB101",
                        "Java Developer",
                        "OPEN"
                );

        when(candidateRepository
                .findByEmailIgnoreCase("john.doe@company.com"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmployeeId("EMP101"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmailIgnoreCaseAndJobId(
                        "john.doe@company.com", 1L))
                .thenReturn(Optional.empty());

        when(candidateRepository
                .findByEmployeeIdAndJobId(
                        "EMP101", 1L))
                .thenReturn(Optional.empty());

        when(jobServiceClient.getJobById(1L))
                .thenReturn(openJob);

        when(candidateRepository.save(any(Candidate.class)))
                .thenReturn(candidate);

        Candidate saved =
                candidateService.applyForJob(candidate);

        assertNotNull(saved);
        assertEquals("John", saved.getFirstName());

        verify(candidateRepository, times(1))
                .save(candidate);
    }

    @Test
    public void testLoginEmployee_Success() {

        Candidate candidate = new Candidate(
                1L,
                "Naman",
                "Dheer",
                "E901",
                "1995-01-01",
                "naman@company.com",
                "Test@123",
                1L
        );

        when(candidateRepository
                .findByEmailIgnoreCase("naman@company.com"))
                .thenReturn(List.of(candidate));

        Candidate loggedIn =
                candidateService.loginEmployee(
                        "naman@company.com",
                        "Test@123"
                );

        assertNotNull(loggedIn);
        assertEquals(
                "naman@company.com",
                loggedIn.getEmail()
        );
    }

    @Test
    public void testDeleteCandidate_Success() {

        Candidate candidate = new Candidate(
                10L,
                "Jane",
                "Doe",
                "EMP202",
                "1996-06-16",
                "jane.doe@company.com",
                "Test@123",
                1L
        );

        when(candidateRepository.findById(10L))
                .thenReturn(Optional.of(candidate));

        candidateService.deleteCandidate(10L);

        verify(notificationRepository, times(1))
                .deleteByCandidateId(10L);

        verify(interviewRepository, times(1))
                .deleteByCandidateId(10L);

        verify(candidateRepository, times(1))
                .deleteById(10L);
    }


    // NEGATIVE TESTS

    @Test
    public void testApplyForJob_InvalidEmailDomain() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@gmail.com",
                "Test@123",
                1L
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Only company email addresses ending with @company.com are allowed"
                )
        );
    }

    @Test
    public void testApplyForJob_MissingEmail() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                null,
                "Test@123",
                1L
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains("Email is required")
        );
    }

    @Test
    public void testApplyForJob_MissingEmployeeId() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                null,
                "1995-05-15",
                "john.doe@company.com",
                "Test@123",
                1L
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Employee ID is required"
                )
        );
    }

    @Test
    public void testApplyForJob_MissingPasswordForNewCandidate() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@company.com",
                null,
                1L
        );

        when(candidateRepository
                .findByEmailIgnoreCase("john.doe@company.com"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmployeeId("EMP101"))
                .thenReturn(List.of());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Password is required"
                )
        );
    }

    @Test
    public void testApplyForJob_DuplicateApplicationForSameJob() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@company.com",
                "Test@123",
                1L
        );

        when(candidateRepository
                .findByEmailIgnoreCase("john.doe@company.com"))
                .thenReturn(List.of(candidate));

        when(candidateRepository
                .findByEmployeeId("EMP101"))
                .thenReturn(List.of(candidate));

        when(candidateRepository
                .findByEmailIgnoreCaseAndJobId(
                        "john.doe@company.com", 1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "already applied"
                )
        );
    }

    @Test
    public void testApplyForJob_JobDoesNotExist() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@company.com",
                "Test@123",
                999L
        );

        when(candidateRepository
                .findByEmailIgnoreCase("john.doe@company.com"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmployeeId("EMP101"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmailIgnoreCaseAndJobId(
                        "john.doe@company.com", 999L))
                .thenReturn(Optional.empty());

        when(candidateRepository
                .findByEmployeeIdAndJobId(
                        "EMP101", 999L))
                .thenReturn(Optional.empty());

        when(jobServiceClient.getJobById(999L))
                .thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "does not exist"
                )
        );
    }

    @Test
    public void testApplyForJob_ClosedJob() {

        Candidate candidate = new Candidate(
                null,
                "John",
                "Doe",
                "EMP101",
                "1995-05-15",
                "john.doe@company.com",
                "Test@123",
                2L
        );

        JobPostingDto closedJob =
                new JobPostingDto(
                        2L,
                        "JOB102",
                        "Java Developer",
                        "CLOSED"
                );

        when(candidateRepository
                .findByEmailIgnoreCase("john.doe@company.com"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmployeeId("EMP101"))
                .thenReturn(List.of());

        when(candidateRepository
                .findByEmailIgnoreCaseAndJobId(
                        "john.doe@company.com", 2L))
                .thenReturn(Optional.empty());

        when(candidateRepository
                .findByEmployeeIdAndJobId(
                        "EMP101", 2L))
                .thenReturn(Optional.empty());

        when(jobServiceClient.getJobById(2L))
                .thenReturn(closedJob);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.applyForJob(candidate)
        );

        assertTrue(
                exception.getMessage().contains(
                        "CLOSED"
                )
        );
    }

    @Test
    public void testLoginEmployee_WrongPassword() {

        Candidate candidate = new Candidate(
                1L,
                "Naman",
                "Dheer",
                "E901",
                "1995-01-01",
                "naman@company.com",
                "Test@123",
                1L
        );

        when(candidateRepository
                .findByEmailIgnoreCase("naman@company.com"))
                .thenReturn(List.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.loginEmployee(
                        "naman@company.com",
                        "Wrong123"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Invalid email"
                )
        );
    }

    @Test
    public void testLoginEmployee_InvalidDomain() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.loginEmployee(
                        "naman@gmail.com",
                        "Test@123"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Only company email addresses ending with @company.com are allowed"
                )
        );
    }

    @Test
    public void testLoginEmployee_EmailNotFound() {

        when(candidateRepository
                .findByEmailIgnoreCase("unknown@company.com"))
                .thenReturn(List.of());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.loginEmployee(
                        "unknown@company.com",
                        "Test@123"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Invalid email"
                )
        );
    }

    @Test
    public void testDeleteCandidate_NotFound() {

        when(candidateRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateService.deleteCandidate(999L)
        );

        assertTrue(
                exception.getMessage().contains(
                        "not found"
                )
        );

        verify(candidateRepository, times(1))
                .findById(999L);

        verify(candidateRepository, never())
                .deleteById(999L);

        verify(notificationRepository, never())
                .deleteByCandidateId(999L);

        verify(interviewRepository, never())
                .deleteByCandidateId(999L);
    }
}