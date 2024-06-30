package com.eva_karaoke.karaoke.controller;

import com.eva_karaoke.karaoke.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class JobController {

  @Autowired
  private JobService jobService;

  @GetMapping("/jobs")
  public String listJobs(Model model) {
    List<JobInstance> jobInstances = jobService.listJobs();
    model.addAttribute("jobInstances", jobInstances);
    return "jobs";
  }

  @GetMapping("/jobs/{jobName}")
  public String listJobExecutions(@PathVariable String jobName, Model model) {
    List<JobExecution> jobExecutions = jobService.listJobExecutions(jobName);
    model.addAttribute("jobExecutions", jobExecutions);
    return "jobInstances";
  }

  @DeleteMapping("/jobs/{jobInstanceId}")
  public ResponseEntity<String> deleteJobInstance(@PathVariable Long jobInstanceId) {
    String result = jobService.deleteJobInstance(jobInstanceId);
    if (result.contains("successfully")) {
      return new ResponseEntity<>(result, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
