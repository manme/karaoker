package com.eva_karaoke.karaoke.config;

import com.eva_karaoke.karaoke.tasklet.VideoDownloadTasklet;
import com.eva_karaoke.karaoke.tasklet.AudioExtractTasklet;
import com.eva_karaoke.karaoke.tasklet.AudioSeparationTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

@Configuration
@EnableBatchProcessing
public class VideoProcessingJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final VideoDownloadTasklet videoDownloadTasklet;
  private final AudioExtractTasklet audioExtractTasklet;
  private final AudioSeparationTasklet audioSeparationTasklet;
  private final ThreadPoolTaskExecutor taskExecutor;

  public VideoProcessingJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
      VideoDownloadTasklet videoDownloadTasklet, AudioExtractTasklet audioExtractTasklet,
      AudioSeparationTasklet audioSeparationTasklet, ThreadPoolTaskExecutor taskExecutor) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.videoDownloadTasklet = videoDownloadTasklet;
    this.audioExtractTasklet = audioExtractTasklet;
    this.audioSeparationTasklet = audioSeparationTasklet;
    this.taskExecutor = taskExecutor;
  }

  @Bean
  public Job videoProcessingJob() {
    Step downloadStep = new StepBuilder("downloadVideoStep", jobRepository)
        .tasklet(videoDownloadTasklet, transactionManager)
        .build();

    Step extractAudioStep = new StepBuilder("extractAudioStep", jobRepository)
        .tasklet(audioExtractTasklet, transactionManager)
        .build();

    Step separateAudioStep = new StepBuilder("separateAudioStep", jobRepository)
        .tasklet(audioSeparationTasklet, transactionManager)
        .build();

    return new JobBuilder("videoProcessingJob", jobRepository)
        .start(downloadStep)
        .next(extractAudioStep)
        .next(separateAudioStep)
        .incrementer(new RunIdIncrementer())
        .build();
  }
}
