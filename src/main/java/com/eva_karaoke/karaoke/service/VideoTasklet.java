package com.eva_karaoke.karaoke.service;

import com.eva_karaoke.karaoke.model.Video;
import com.eva_karaoke.karaoke.repository.VideoRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VideoTasklet implements Tasklet {

  @Autowired
  private VideoRepository videoRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    Long videoId = (Long) chunkContext.getStepContext().getJobParameters().get("videoId");
    Optional<Video> optionalVideo = videoRepository.findById(videoId);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      processVideo(video);
    }
    return RepeatStatus.FINISHED;
  }

  private void processVideo(Video video) {
    // Implement your video processing logic here
  }
}
