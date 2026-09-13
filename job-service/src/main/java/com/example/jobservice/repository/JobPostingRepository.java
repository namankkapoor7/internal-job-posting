package com.example.jobservice.repository;

import com.example.jobservice.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByStatus(String status);

    @Query("SELECT j FROM JobPosting j WHERE " +
           "(:status IS NULL OR LOWER(j.status) = LOWER(:status)) AND " +
           "(:designation IS NULL OR :designation = '' OR LOWER(j.designation) LIKE LOWER(CONCAT('%', :designation, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:skill IS NULL OR :skill = '' OR LOWER(j.skillSet) LIKE LOWER(CONCAT('%', :skill, '%')))")
    List<JobPosting> filterJobs(@Param("status") String status,
                                @Param("designation") String designation,
                                @Param("location") String location,
                                @Param("skill") String skill);
}
