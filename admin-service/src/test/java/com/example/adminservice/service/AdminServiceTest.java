package com.example.adminservice.service;

import com.example.adminservice.entity.Admin;
import com.example.adminservice.entity.Designation;
import com.example.adminservice.repository.AdminRepository;
import com.example.adminservice.repository.DesignationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================
    // POSITIVE TEST CASES
    // =========================================================

    @Test
    public void testValidateLogin_Success() {
        Admin admin = new Admin(1L, "admin@company.com", "admin123");
        when(adminRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(admin));

        boolean isValid = adminService.validateLogin("admin@company.com", "admin123");

        assertTrue(isValid);
        verify(adminRepository, times(1)).findByEmail("admin@company.com");
    }

    @Test
    public void testAddDesignation_Success() {
        Designation desig = new Designation(null, "DevOps Engineer", "ACTIVE");
        when(designationRepository.findByNameIgnoreCase("DevOps Engineer")).thenReturn(Optional.empty());
        when(designationRepository.save(any(Designation.class))).thenAnswer(i -> i.getArgument(0));

        Designation saved = adminService.addDesignation(desig);

        assertNotNull(saved);
        assertEquals("DevOps Engineer", saved.getName());
        assertEquals("ACTIVE", saved.getStatus());
        verify(designationRepository, times(1)).save(any(Designation.class));
    }

    // =========================================================
    // NEGATIVE TEST CASES (Null, Blank, Malformed & Side Effects)
    // =========================================================

    @Test
    public void testValidateLogin_NullEmail() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.validateLogin(null, "password123")
        );

        assertEquals("Email is required!", ex.getMessage());
        verify(adminRepository, never()).findByEmail(any());
    }

    @Test
    public void testValidateLogin_NullPassword() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.validateLogin("admin@company.com", null)
        );

        assertEquals("Password is required!", ex.getMessage());
        verify(adminRepository, never()).findByEmail(any());
    }

    @Test
    public void testValidateLogin_MalformedEmail() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.validateLogin("notanemail", "password123")
        );

        assertEquals("Invalid email format!", ex.getMessage());
        verify(adminRepository, never()).findByEmail(any());
    }

    @Test
    public void testValidateLogin_InvalidEmailDomain() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.validateLogin("admin@gmail.com", "password123")
        );

        assertEquals("Only company email addresses ending with @company.com are allowed.", ex.getMessage());
        verify(adminRepository, never()).findByEmail(any());
    }

    @Test
    public void testAddDesignation_NullDesignation() {
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.addDesignation(null)
        );

        assertEquals("Designation object cannot be null!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testAddDesignation_EmptyName() {
        Designation desig = new Designation(null, "   ", "ACTIVE");

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.addDesignation(desig)
        );

        assertEquals("Designation name is required!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testAddDesignation_DuplicateName() {
        Designation desig = new Designation(null, "QA Engineer", "ACTIVE");
        when(designationRepository.findByNameIgnoreCase("QA Engineer"))
            .thenReturn(Optional.of(new Designation(1L, "QA Engineer", "ACTIVE")));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.addDesignation(desig)
        );

        assertEquals("Designation 'QA Engineer' already exists!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testUpdateDesignation_NotFound() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.updateDesignation(99L, new Designation(null, "Test", "ACTIVE"))
        );

        assertEquals("Designation with ID 99 not found!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testAddDesignation_TooShortName() {
        Designation desig = new Designation(null, "A", "ACTIVE");

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.addDesignation(desig)
        );

        assertEquals("Designation name must be at least 2 characters long!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testAddDesignation_InvalidCharacters() {
        Designation desig = new Designation(null, "Developer<Script>", "ACTIVE");

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.addDesignation(desig)
        );

        assertEquals("Designation name contains invalid characters!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }

    @Test
    public void testUpdateDesignation_InvalidCharacters() {
        Designation existing = new Designation(1L, "QA Lead", "ACTIVE");
        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));

        Designation update = new Designation(null, "QA Lead @#$", "ACTIVE");

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> adminService.updateDesignation(1L, update)
        );

        assertEquals("Designation name contains invalid characters!", ex.getMessage());
        verify(designationRepository, never()).save(any());
    }
}