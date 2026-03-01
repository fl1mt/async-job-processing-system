package com.fl1mt.jobservice.api;


import com.fl1mt.jobservice.domain.CreateJobRequest;
import com.fl1mt.jobservice.domain.JobResponse;
import com.fl1mt.jobservice.domain.JobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request){
        return ResponseEntity.ok(jobService.createJob(request));
    }

    @GetMapping
    public List<JobResponse> getJobs(){
        return jobService.getJobs();
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable(name = "jobId") Long jobId){
        return ResponseEntity.ok(jobService.getJob(jobId));
    }
}
