package com.example.candidateservice.repository;

import com.example.candidateservice.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    Optional<EmployeeProfile> findByEmailIgnoreCase(String email);
    Optional<EmployeeProfile> findByEmployeeId(String employeeId);
}
