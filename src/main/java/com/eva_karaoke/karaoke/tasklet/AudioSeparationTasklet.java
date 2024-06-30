package com.eva_karaoke.karaoke.tasklet;

import com.eva_karaoke.karaoke.model.Video;
import com.eva_karaoke.karaoke.repository.VideoRepository;
import com.eva_karaoke.karaoke.util.CommandExecutor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AudioSeparationTasklet implements Tasklet {

  @Autowired
  private VideoRepository videoRepository;

  @Autowired
  private CommandExecutor commandExecutor;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    Long videoId = (Long) chunkContext.getStepContext().getJobParameters().get("videoId");
    Optional<Video> optionalVideo = videoRepository.findById(videoId);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      commandExecutor
          .runCommand("scripts/demucs_command.sh --out " + video.getPathSpleeterFolder() + " --two-stems=vocals --"
              + video.getAudioFileExt() + " -n mdx_extra_q --mp3-bitrate 320 --mp3-preset 2 -d cpu "
              + video.getPathAudioFile());
    }
    return RepeatStatus.FINISHED;
  }
}
