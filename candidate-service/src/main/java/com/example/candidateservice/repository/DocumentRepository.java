package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEmployeeIdOrderByUploadedAtDesc(String employeeId);
    Optional<Document> findByIdAndEmployeeId(Long id, String employeeId);
}
