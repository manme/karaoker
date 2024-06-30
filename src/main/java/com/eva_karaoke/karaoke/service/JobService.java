package com.eva_karaoke.karaoke.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

  @Autowired
  private JobExplorer jobExplorer;

  @Autowired
  private JobOperator jobOperator;

  @Autowired
  private JobRepository jobRepository;

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job videoProcessingJob;

  public List<JobInstance> listJobs() {
    List<String> jobNames = jobExplorer.getJobNames();
    return jobNames.stream()
        .flatMap(jobName -> jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE).stream())
        .collect(Collectors.toList());
  }

  public List<JobExecution> listJobExecutions(String jobName) {
    List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);
    return jobInstances.stream()
        .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
        .collect(Collectors.toList());
  }

  public String deleteJobInstance(Long jobInstanceId) {
    try {
      JobInstance jobInstance = jobExplorer.getJobInstance(jobInstanceId);
      if (jobInstance != null) {
        List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
        for (JobExecution jobExecution : jobExecutions) {
          jobOperator.abandon(jobExecution.getId());
          jobRepository.deleteStepExecution(jobExecution.getStepExecutions().iterator().next());
          jobRepository.deleteJobExecution(jobExecution);
        }
        jobRepository.deleteJobInstance(jobInstance);
        return "Job instance deleted successfully";
      } else {
        return "Job instance not found";
      }
    } catch (Exception e) {
      return "Failed to delete job instance: " + e.getMessage();
    }
  }

  public List<JobInstance> listJobInstances(String jobName) {
    return jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);
  }

  public String stopJob(Long jobExecutionId) {
    try {
      jobOperator.stop(jobExecutionId);
      return "Job stopped successfully";
    } catch (Exception e) {
      return "Failed to stop job: " + e.getMessage();
    }
  }

  public String removeJobExecution(Long jobExecutionId) {
    try {
      JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
      if (jobExecution != null) {
        jobOperator.abandon(jobExecutionId);
        jobExecution.getStepExecutions().forEach(stepExecution -> {
          jobRepository.deleteStepExecution(stepExecution);
        });
        jobRepository.deleteJobExecution(jobExecution);
        jobRepository.deleteJobInstance(jobExecution.getJobInstance());
        return "Job execution removed successfully";
      } else {
        return "Job execution not found";
      }
    } catch (Exception e) {
      return "Failed to remove job execution: " + e.getMessage();
    }
  }

  @Async
  public void startVideoProcessingJob(Long videoId) {
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("videoId", videoId)
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      System.out.println("Joblauncher: " + videoId);

      jobLauncher.run(videoProcessingJob, params);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
