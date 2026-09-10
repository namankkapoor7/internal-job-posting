package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCandidateIdOrderByIdDesc(Long candidateId);

    List<Notification> findByCandidateIdInOrderByIdDesc(List<Long> candidateIds);

    long countByCandidateIdAndIsReadFalse(Long candidateId);

    long countByCandidateIdInAndIsReadFalse(List<Long> candidateIds);

    @Transactional
    void deleteByCandidateId(Long candidateId);
}
