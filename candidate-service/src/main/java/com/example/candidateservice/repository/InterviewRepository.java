package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplicationId(Long applicationId);
    List<Interview> findByEmployeeId(String employeeId);
    List<Interview> findByJobId(Long jobId);
    void deleteByApplicationId(Long applicationId);
}
