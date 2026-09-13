package com.example.adminservice.controller;

import com.example.adminservice.entity.Admin;
import com.example.adminservice.entity.Designation;
import com.example.adminservice.security.JwtUtil;
import com.example.adminservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest != null ? loginRequest.get("email") : null;
        String password = loginRequest != null ? loginRequest.get("password") : null;

        Map<String, String> response = new HashMap<>();

        if (email == null || password == null) {
            response.put("message", "Email and password are required.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String emailTrimmed = email.trim();
        if (!emailTrimmed.toLowerCase().endsWith("@company.com")) {
            response.put("message", "Only company email addresses ending with @company.com are allowed.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            if (adminService.validateLogin(emailTrimmed, password)) {
                Optional<Admin> adminOpt = adminService.findByEmail(emailTrimmed);
                Long id = adminOpt.map(Admin::getId).orElse(1L);
                String token = jwtUtil.generateToken(emailTrimmed, "ADMIN", "HR001", id, "HR Admin");

                response.put("message", "Login successful");
                response.put("status", "SUCCESS");
                response.put("token", token);
                response.put("role", "ADMIN");
                response.put("email", emailTrimmed);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "Invalid email or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (RuntimeException e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Designation Master Endpoints
    @PostMapping("/designations")
    public ResponseEntity<?> addDesignation(@RequestBody Designation designation) {
        try {
            Designation created = adminService.addDesignation(designation);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/designations")
    public ResponseEntity<List<Designation>> getAllDesignations() {
        List<Designation> list = adminService.getAllDesignations();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/designations/active")
    public ResponseEntity<List<Designation>> getActiveDesignations() {
        List<Designation> activeList = adminService.getActiveDesignations();
        return new ResponseEntity<>(activeList, HttpStatus.OK);
    }

    @PutMapping("/designations/{id}")
    public ResponseEntity<?> updateDesignation(@PathVariable Long id, @RequestBody Designation designation) {
        try {
            Designation updated = adminService.updateDesignation(id, designation);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/designations/{id}/status")
    public ResponseEntity<?> updateDesignationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body != null ? body.get("status") : null;
            Designation updated = adminService.updateDesignationStatus(id, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
