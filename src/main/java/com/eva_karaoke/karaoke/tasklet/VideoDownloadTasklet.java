package com.eva_karaoke.karaoke.tasklet;

import com.eva_karaoke.karaoke.model.Video;
import com.eva_karaoke.karaoke.repository.VideoRepository;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class VideoDownloadTasklet implements Tasklet {

  @Autowired
  private VideoRepository videoRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    Long videoId = (Long) chunkContext.getStepContext().getJobParameters().get("videoId");
    Optional<Video> optionalVideo = videoRepository.findById(videoId);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      runCommand("yt-dlp -f \"bestvideo+bestaudio[ext=m4a]\" --merge-output-format " + video.getVideoFileExt()
          + " -o " + video.getPathVideoFile() + " " + video.getUrl(), video);
    }

    return RepeatStatus.FINISHED;
  }

  private void runCommand(String command, Video video) throws ExecuteException, IOException {
    System.out.println("Command:\n" + command);

    CommandLine cmdLine = CommandLine.parse(command);
    DefaultExecutor executor = new DefaultExecutor();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);

    AtomicBoolean processComplete = new AtomicBoolean(false);
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Runnable outputLogger = () -> {
      String output = outputStream.toString();
      if (!output.isEmpty()) {
        System.out.println("Command Output:\n" + output);
        outputStream.reset();
      }
    };

    // Schedule the output logger to run every 5 seconds
    scheduler.scheduleAtFixedRate(outputLogger, 0, 5, TimeUnit.SECONDS);

    try {
      executor.execute(cmdLine);
      processComplete.set(true);
    } finally {
      scheduler.shutdown();
      // Log any remaining output after process completion
      outputLogger.run();

      System.out.println("Finished:\n" + command);
    }
  }
}
