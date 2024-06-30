package com.eva_karaoke.karaoke.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VideoTest {

  @Test
  public void testConvertToSeconds() {
    Video video = new Video();

    assertEquals(31, video.convertToSeconds("31"));
    assertEquals(390, video.convertToSeconds("6:30"));
    assertEquals(9620, video.convertToSeconds("2:40:20"));
    assertEquals(3661, video.convertToSeconds("1:01:01"));
    assertEquals(0, video.convertToSeconds("0"));
    assertEquals(59, video.convertToSeconds("59"));
    assertEquals(3600, video.convertToSeconds("1:00:00"));
  }
}
