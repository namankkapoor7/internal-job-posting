package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeIdOrderByIdDesc(String employeeId);
    long countByEmployeeIdAndIsReadFalse(String employeeId);
    void deleteByEmployeeId(String employeeId);
}
