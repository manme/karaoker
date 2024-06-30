package com.eva_karaoke.karaoke.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

import com.eva_karaoke.karaoke.model.Video;
import com.eva_karaoke.karaoke.repository.VideoRepository;
import com.eva_karaoke.karaoke.service.VideoProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test-containers")
@Transactional
public class VideoControllerIntegrationTest {

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("karaoker_test")
      .withUsername("postgres")
      .withPassword("postgres");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private VideoRepository videoRepository;

  @Autowired
  private VideoProcessingService videoProcessingService;

  @Test
  public void testCreateVideo() throws Exception {
    // Create an ObjectNode and set its attributes
    ObjectNode videoJson = objectMapper.createObjectNode();
    videoJson.put("url", "http://example.com/video.mp4");
    videoJson.put("title", "Sample Video");
    videoJson.put("thumbnailUrl", "http://example.com/thumbnail.jpg");
    videoJson.put("length", "2:40:20");

    // Perform the POST request and verify the response
    mockMvc.perform(post("/videos")
        .contentType("application/json")
        .content(videoJson.toString()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.url", is(videoJson.get("url").asText())))
        .andExpect(jsonPath("$.title", is(videoJson.get("title").asText())))
        .andExpect(jsonPath("$.thumbnailUrl", is(videoJson.get("thumbnailUrl").asText())))
        .andExpect(jsonPath("$.length", is(videoJson.get("length").asText())))
        .andExpect(jsonPath("$.id", notNullValue())); // Assuming the server generates an ID
  }
}
