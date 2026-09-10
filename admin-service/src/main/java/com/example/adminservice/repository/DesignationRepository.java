package com.example.adminservice.repository;

import com.example.adminservice.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    List<Designation> findByStatus(String status);

    Optional<Designation> findByName(String name);

    Optional<Designation> findByNameIgnoreCase(String name);
}
