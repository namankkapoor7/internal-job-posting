package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByEmail(String email);

    List<Candidate> findByEmailIgnoreCase(String email);

    List<Candidate> findByEmployeeId(String employeeId);

    /*
     * Returns only actual job applications.
     *
     * jobId = 0 means employee profile/registration.
     * jobId > 0 means actual job application.
     */
    List<Candidate> findByEmployeeIdAndJobIdGreaterThan(
            String employeeId,
            Long jobId
    );

    Optional<Candidate> findByEmailAndJobId(
            String email,
            Long jobId
    );

    Optional<Candidate> findByEmailIgnoreCaseAndJobId(
            String email,
            Long jobId
    );

    Optional<Candidate> findByEmployeeIdAndJobId(
            String employeeId,
            Long jobId
    );

    List<Candidate> findByJobId(Long jobId);
}