package com.example.adminservice.service;

import com.example.adminservice.entity.Admin;
import com.example.adminservice.entity.Designation;
import com.example.adminservice.repository.AdminRepository;
import com.example.adminservice.repository.DesignationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void initSampleData() {
        if (adminRepository.findByEmail("admin@company.com").isEmpty()) {
            Admin admin = new Admin(null, "admin@company.com", passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
        }

        if (designationRepository.count() == 0) {
            List<String> defaultDesignations = Arrays.asList(
                    "Java Full Stack Developer",
                    "Angular Developer",
                    "Full Stack Developer",
                    "Backend Engineer",
                    "QA Engineer",
                    "DevOps Lead",
                    "Product Owner"
            );
            for (String name : defaultDesignations) {
                designationRepository.save(new Designation(null, name, "ACTIVE"));
            }
        }
    }

    public boolean validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required!");
        }
        String emailTrimmed = email.trim().toLowerCase();
        if (!emailTrimmed.contains("@")) {
            throw new RuntimeException("Invalid email format!");
        }
        if (!emailTrimmed.endsWith("@company.com")) {
            throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
        }
        Optional<Admin> adminOpt = adminRepository.findByEmail(emailTrimmed);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(password.trim(), admin.getPassword()) || password.trim().equals(admin.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email.trim());
    }

    private void validateDesignationName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Designation name is required!");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            throw new RuntimeException("Designation name must be at least 2 characters long!");
        }
        if (!trimmed.matches("^[a-zA-Z0-9\\s&/-]+$")) {
            throw new RuntimeException("Designation name contains invalid characters!");
        }
    }

    // Designation Master Management
    public Designation addDesignation(Designation designation) {
        if (designation == null) {
            throw new RuntimeException("Designation object cannot be null!");
        }
        validateDesignationName(designation.getName());
        String name = designation.getName().trim();
        Optional<Designation> existing = designationRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            throw new RuntimeException("Designation '" + name + "' already exists!");
        }
        if (designation.getStatus() == null || designation.getStatus().trim().isEmpty()) {
            designation.setStatus("ACTIVE");
        }
        designation.setName(name);
        return designationRepository.save(designation);
    }

    public List<Designation> getAllDesignations() {
        return designationRepository.findAll();
    }

    public List<Designation> getActiveDesignations() {
        return designationRepository.findByStatus("ACTIVE");
    }

    public Designation updateDesignation(Long id, Designation updated) {
        Optional<Designation> optional = designationRepository.findById(id);
        if (optional.isPresent()) {
            Designation designation = optional.get();
            if (updated.getName() != null && !updated.getName().trim().isEmpty()) {
                validateDesignationName(updated.getName());
                String newName = updated.getName().trim();
                Optional<Designation> existing = designationRepository.findByNameIgnoreCase(newName);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new RuntimeException("Designation '" + newName + "' already exists!");
                }
                designation.setName(newName);
            }
            if (updated.getStatus() != null && !updated.getStatus().trim().isEmpty()) {
                designation.setStatus(updated.getStatus().trim());
            }
            return designationRepository.save(designation);
        }
        throw new RuntimeException("Designation with ID " + id + " not found!");
    }

    public Designation updateDesignationStatus(Long id, String status) {
        Optional<Designation> optional = designationRepository.findById(id);
        if (optional.isPresent()) {
            Designation designation = optional.get();
            designation.setStatus(status.trim());
            return designationRepository.save(designation);
        }
        throw new RuntimeException("Designation with ID " + id + " not found!");
    }
}
