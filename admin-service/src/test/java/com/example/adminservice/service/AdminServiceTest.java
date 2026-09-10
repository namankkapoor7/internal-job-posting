package com.example.adminservice.service;

import com.example.adminservice.entity.Admin;
import com.example.adminservice.entity.Designation;
import com.example.adminservice.repository.AdminRepository;
import com.example.adminservice.repository.DesignationRepository;
import com.example.adminservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
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

    // =========================
    // POSITIVE TESTS
    // =========================

    @Test
    public void testValidateLogin_ValidCredentials() {

        Admin admin =
                new Admin(
                        1L,
                        "admin@company.com",
                        "admin123"
                );

        when(adminRepository
                .findByEmailAndPassword(
                        "admin@company.com",
                        "admin123"
                ))
                .thenReturn(Optional.of(admin));

        boolean isValid =
                adminService.validateLogin(
                        "admin@company.com",
                        "admin123"
                );

        assertTrue(isValid);

        verify(adminRepository, times(1))
                .findByEmailAndPassword(
                        "admin@company.com",
                        "admin123"
                );
    }

    @Test
    public void testValidateLogin_InvalidCredentials() {

        when(adminRepository
                .findByEmailAndPassword(
                        "admin@company.com",
                        "wrongpass"
                ))
                .thenReturn(Optional.empty());

        boolean isValid =
                adminService.validateLogin(
                        "admin@company.com",
                        "wrongpass"
                );

        assertFalse(isValid);

        verify(adminRepository, times(1))
                .findByEmailAndPassword(
                        "admin@company.com",
                        "wrongpass"
                );
    }

    @Test
    public void testAddDesignation_Success() {

        Designation desig =
                new Designation(
                        null,
                        "Spring Boot Developer",
                        "ACTIVE"
                );

        when(designationRepository
                .findByNameIgnoreCase(
                        "Spring Boot Developer"
                ))
                .thenReturn(Optional.empty());

        when(designationRepository
                .save(any(Designation.class)))
                .thenReturn(
                        new Designation(
                                1L,
                                "Spring Boot Developer",
                                "ACTIVE"
                        )
                );

        Designation created =
                adminService.addDesignation(desig);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals(
                "Spring Boot Developer",
                created.getName()
        );
    }

    @Test
    public void testGetActiveDesignations() {

        Designation d1 =
                new Designation(
                        1L,
                        "Java Developer",
                        "ACTIVE"
                );

        when(designationRepository
                .findByStatus("ACTIVE"))
                .thenReturn(Arrays.asList(d1));

        List<Designation> activeList =
                adminService.getActiveDesignations();

        assertEquals(1, activeList.size());
        assertEquals(
                "Java Developer",
                activeList.get(0).getName()
        );
    }


    // NEGATIVE TESTS

    @Test
    public void testValidateLogin_InvalidEmailDomain() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.validateLogin(
                                "admin@gmail.com",
                                "admin123"
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "@company.com"
                )
        );

        verify(adminRepository, never())
                .findByEmailAndPassword(
                        anyString(),
                        anyString()
                );
    }

    @Test
    public void testAddDesignation_EmptyName() {

        Designation desig =
                new Designation(
                        null,
                        "   ",
                        "ACTIVE"
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.addDesignation(desig)
                );

        assertTrue(
                exception.getMessage().contains(
                        "Designation name is required"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }

    @Test
    public void testAddDesignation_NullName() {

        Designation desig =
                new Designation(
                        null,
                        null,
                        "ACTIVE"
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.addDesignation(desig)
                );

        assertTrue(
                exception.getMessage().contains(
                        "Designation name is required"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }

    @Test
    public void testAddDesignation_DuplicateName() {

        Designation desig =
                new Designation(
                        null,
                        "Spring Boot Developer",
                        "ACTIVE"
                );

        when(designationRepository
                .findByNameIgnoreCase(
                        "Spring Boot Developer"
                ))
                .thenReturn(
                        Optional.of(
                                new Designation(
                                        1L,
                                        "Spring Boot Developer",
                                        "ACTIVE"
                                )
                        )
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.addDesignation(desig)
                );

        assertTrue(
                exception.getMessage().contains(
                        "already exists"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }

    @Test
    public void testUpdateDesignation_NotFound() {

        Designation updated =
                new Designation(
                        null,
                        "Senior Developer",
                        "ACTIVE"
                );

        when(designationRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.updateDesignation(
                                999L,
                                updated
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "not found"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }

    @Test
    public void testUpdateDesignation_DuplicateName() {

        Designation existing =
                new Designation(
                        2L,
                        "Angular Developer",
                        "ACTIVE"
                );

        Designation duplicate =
                new Designation(
                        null,
                        "Java Developer",
                        "ACTIVE"
                );

        Designation anotherExisting =
                new Designation(
                        1L,
                        "Java Developer",
                        "ACTIVE"
                );

        when(designationRepository.findById(2L))
                .thenReturn(Optional.of(existing));

        when(designationRepository
                .findByNameIgnoreCase("Java Developer"))
                .thenReturn(Optional.of(anotherExisting));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.updateDesignation(
                                2L,
                                duplicate
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "already exists"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }

    @Test
    public void testUpdateDesignationStatus_NotFound() {

        when(designationRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> adminService.updateDesignationStatus(
                                999L,
                                "INACTIVE"
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "not found"
                )
        );

        verify(designationRepository, never())
                .save(any(Designation.class));
    }
}