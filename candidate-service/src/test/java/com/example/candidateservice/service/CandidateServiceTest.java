package com.example.candidateservice.service;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.*;
import com.example.candidateservice.repository.*;
import com.example.candidateservice.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CandidateServiceTest {

    @Mock
    private EmployeeProfileRepository employeeProfileRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Spy
    private JwtUtil jwtUtil = new JwtUtil();

    @InjectMocks
    private CandidateService candidateService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================
    // POSITIVE TEST CASES
    // =========================================================

    @Test
    public void testRegisterEmployee_Success() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        when(employeeProfileRepository.findByEmailIgnoreCase("john.doe@company.com")).thenReturn(Optional.empty());
        when(employeeProfileRepository.findByEmployeeId("EMP101")).thenReturn(Optional.empty());
        when(employeeProfileRepository.save(any(EmployeeProfile.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeProfile saved = candidateService.registerEmployee(profile);

        assertNotNull(saved);
        assertEquals("EMP101", saved.getEmployeeId());
        assertEquals("john.doe@company.com", saved.getEmail());
        verify(employeeProfileRepository, times(1)).save(any(EmployeeProfile.class));
    }

    @Test
    public void testLoginEmployee_Success() {
        EmployeeProfile profile = new EmployeeProfile(
            1L, "EMP101", "john.doe@company.com", encoder.encode("Password123"),
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        when(employeeProfileRepository.findByEmailIgnoreCase("john.doe@company.com")).thenReturn(Optional.of(profile));

        Map<String, Object> result = candidateService.loginEmployee("john.doe@company.com", "Password123");

        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertEquals("EMP101", result.get("employeeId"));
    }

    @Test
    public void testApplyForJob_Success() {
        EmployeeProfile profile = new EmployeeProfile(
            1L, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Senior Java Developer", "PUBLISHED");

        when(employeeProfileRepository.findByEmployeeId("EMP101")).thenReturn(Optional.of(profile));
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(applicationRepository.findByEmployeeIdAndJobIdAndStatusNotIn(eq("EMP101"), eq(1L), any())).thenReturn(List.of());
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application app = candidateService.applyForJob("EMP101", 1L, "Looking forward", null);

        assertNotNull(app);
        assertEquals("EMP101", app.getEmployeeId());
        assertEquals("SUBMITTED", app.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testWithdrawApplication_Success() {
        Application app = new Application(10L, "EMP101", 1L, "Title", "Senior Java Developer", "Bangalore", "Engineering", "Note", null);
        app.setStatus("SUBMITTED");

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application withdrawn = candidateService.withdrawApplication(10L, "EMP101");

        assertNotNull(withdrawn);
        assertEquals("WITHDRAWN", withdrawn.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // =========================================================
    // NEGATIVE TEST CASES (Null, Blank, Malformed, Boundary, Side Effects)
    // =========================================================

    @Test
    public void testRegisterEmployee_NullProfile() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(null)
        );

        assertEquals("Employee profile object cannot be null!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_MissingFirstName() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("First Name is required!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_MalformedEmail() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "notanemail", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Invalid email format!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_InvalidEmailDomain() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@gmail.com", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Only company email addresses ending with @company.com are allowed.", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_NegativeExperience() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", -2.5
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Experience years must be between 0 and 60!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_FirstNameWithNumbers() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John123", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_LastNameWithSpecialChars() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe@#$", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Last Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_ShortPassword() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Password must be at least 6 characters long!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_InvalidEmployeeIdFormat() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP 101!", "john.doe@company.com", "Password123",
            "John", "Doe", "1995-05-15", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Employee ID must contain only alphanumeric characters, underscores, or hyphens!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_FutureDob() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "2099-01-01", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Date of Birth cannot be in the future!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_UnderageDob() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "2020-01-01", "Java Developer", "Engineering", "Java", 3.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Candidate must be at least 18 years old!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testRegisterEmployee_ExcessiveExperience() {
        EmployeeProfile profile = new EmployeeProfile(
            null, "EMP101", "john.doe@company.com", "Password123",
            "John", "Doe", "1980-01-01", "Java Developer", "Engineering", "Java", 65.0
        );

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.registerEmployee(profile)
        );

        assertEquals("Experience years must be between 0 and 60!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testUpdateProfile_InvalidFirstName() {
        EmployeeProfile existing = new EmployeeProfile(1L, "EMP101", "john@company.com", "Pass123", "John", "Doe", "1995-05-15", "Desig", "Dept", "Skills", 3.0);
        EmployeeProfile updated = new EmployeeProfile();
        updated.setFirstName("John999");

        when(employeeProfileRepository.findByEmployeeId("EMP101")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.updateProfile("EMP101", updated)
        );

        assertEquals("First Name must contain only alphabetic characters, spaces, hyphens, or apostrophes!", ex.getMessage());
        verify(employeeProfileRepository, never()).save(any());
    }

    @Test
    public void testApplyForJob_ClosedJob() {
        EmployeeProfile profile = new EmployeeProfile(1L, "EMP101", "john@company.com", "P", "J", "D", "1995-05-15", "D", "E", "S", 3.0);
        JobPostingDto closedJob = new JobPostingDto(1L, "JOB101", "Java Developer", "CLOSED");

        when(employeeProfileRepository.findByEmployeeId("EMP101")).thenReturn(Optional.of(profile));
        when(jobServiceClient.getJobById(1L)).thenReturn(closedJob);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.applyForJob("EMP101", 1L, "Note", null)
        );

        assertTrue(ex.getMessage().contains("Cannot apply: Job posting"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    public void testUpdateStage_WithdrawnApplication() {
        Application withdrawnApp = new Application(10L, "EMP101", 1L, "Title", "Designation", "Loc", "Dept", "Note", null);
        withdrawnApp.setStatus("WITHDRAWN");

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(withdrawnApp));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.updateApplicationStageByHR(10L, "SHORTLISTED", "Notes")
        );

        assertEquals("Cannot update stage: Candidate has WITHDRAWN this application.", ex.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    public void testScheduleInterview_WithdrawnApplication() {
        Application withdrawnApp = new Application(10L, "EMP101", 1L, "Title", "Designation", "Loc", "Dept", "Note", null);
        withdrawnApp.setStatus("WITHDRAWN");

        Interview interview = new Interview();
        interview.setApplicationId(10L);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(withdrawnApp));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> candidateService.scheduleInterview(interview)
        );

        assertEquals("Cannot schedule interview: Candidate has WITHDRAWN this application.", ex.getMessage());
        verify(interviewRepository, never()).save(any());
    }
}