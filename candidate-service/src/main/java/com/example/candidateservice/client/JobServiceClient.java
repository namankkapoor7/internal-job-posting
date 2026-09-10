package com.example.candidateservice.client;

import com.example.candidateservice.dto.JobPostingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{id}")
    JobPostingDto getJobById(@PathVariable("id") Long id);
}
