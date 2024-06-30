package com.eva_karaoke.karaoke.service;

import com.eva_karaoke.karaoke.model.Video;
import com.eva_karaoke.karaoke.model.VideoProgressStatus;
import com.eva_karaoke.karaoke.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class VideoCreationService {

  @Autowired
  private VideoRepository videoRepository;

  @Autowired
  private JobService jobService;

  @Transactional
  public Video createAndProcessVideo(Video video) {
    video.setProgressStatus(VideoProgressStatus.STATUS_QUEUED);
    video.setLength(video.getLength());
    Video savedVideo = videoRepository.saveAndFlush(video);
    System.out.println("Video saved with ID: " + savedVideo.getId()); // Logging to verify

    // Register a synchronization callback to execute after the transaction commits
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        System.out.print("Saved video: " + video.getId());
        jobService.startVideoProcessingJob(video.getId());
      }
    });

    return savedVideo;
  }
}
