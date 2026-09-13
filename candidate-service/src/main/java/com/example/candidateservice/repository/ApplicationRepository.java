package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByEmployeeIdOrderBySubmittedAtDesc(String employeeId);
    List<Application> findByJobId(Long jobId);
    Optional<Application> findByEmployeeIdAndJobId(String employeeId, Long jobId);
    List<Application> findByEmployeeIdAndJobIdAndStatusNotIn(String employeeId, Long jobId, List<String> terminalStatuses);
    List<Application> findByStatus(String status);
}
