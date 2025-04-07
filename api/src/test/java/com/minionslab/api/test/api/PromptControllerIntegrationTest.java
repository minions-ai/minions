package com.minionslab.api.test.api;

import static com.minionslab.api.test.util.TestConstants.TEST_COMPONENT_TYPE;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.api.test.config.WithMockMinionUser;
import com.minionslab.api.test.controller.dto.CreatePromptRequest;
import com.minionslab.api.test.controller.dto.PromptComponentRequest;
import com.minionslab.api.test.controller.dto.PromptResponse;
import com.minionslab.api.test.controller.dto.UpdatePromptRequest;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.repository.PromptRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@Tag("integration")
public class PromptControllerIntegrationTest extends BaseControllerIntegrationTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PromptRepository promptRepository;

  @Test
  void getPrompt_Success() {
    // Given
    MinionPrompt savedPrompt = promptRepository.save(context.get(MinionPrompt.class));

    // When
    ResponseEntity<PromptResponse> response = GET(
        BASE_URL + "/prompts/" + savedPrompt.getId(),
        PromptResponse.class,
        PrivilegeLevel.MEDIUM
    );

    // Then
    assertResponseStatus(response, HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(savedPrompt.getId());
    assertThat(response.getBody().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  void updatePrompt_Success() {
    // Given
    MinionPrompt savedPrompt = promptRepository.save(context.get(MinionPrompt.class));
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);

    // When
    ResponseEntity<PromptResponse> response = PUT(
        updateRequest,
        BASE_URL + "/prompts/" + savedPrompt.getId(),
        PromptResponse.class,
        PrivilegeLevel.HIGH
    );

    // Then
    assertResponseStatus(response, HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo("Updated Description");

    // Verify in database
    Optional<MinionPrompt> updatedPrompt = promptRepository.findById(savedPrompt.getId());
    assertThat(updatedPrompt).isPresent();
    assertThat(updatedPrompt.get().getDescription()).isEqualTo("Updated Description");
  }

  @Test
  void deletePrompt_Success() {
    // Given
    MinionPrompt savedPrompt = promptRepository.save(context.get(MinionPrompt.class));

    // When
    ResponseEntity<Void> response = DELETE(
        BASE_URL + "/prompts/" + savedPrompt.getId(),
        Void.class,
        PrivilegeLevel.HIGH
    );

    // Then
    assertResponseStatus(response, HttpStatus.NO_CONTENT);

    // Verify in database
    Optional<MinionPrompt> deletedPrompt = promptRepository.findById(savedPrompt.getId());
    assertThat(deletedPrompt).isEmpty();
  }

  @Test
  void addPromptComponent_Success() {
    // Given
    MinionPrompt savedPrompt = promptRepository.save(context.get(MinionPrompt.class));
    PromptComponentRequest componentRequest = context.get(PromptComponentRequest.class);

    // When
    ResponseEntity<PromptResponse> response = PUT(
        componentRequest,
        BASE_URL + "/prompts/" + savedPrompt.getId() + "/components/"+componentRequest.getType(),
        PromptResponse.class,
        PrivilegeLevel.HIGH
    );

    // Then
    assertResponseStatus(response, HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getComponents()).isNotNull();

    // Verify in database
    Optional<MinionPrompt> updatedPrompt = promptRepository.findById(savedPrompt.getId());
    assertThat(updatedPrompt).isPresent();
    assertThat(updatedPrompt.get().getComponents()).containsKey(TEST_COMPONENT_TYPE);
  }

  @Test
  void unauthorized_Access() {
    // When
    ResponseEntity<String> response = GET(
        BASE_URL + "/prompts",
        String.class,
        PrivilegeLevel.NO
    );

    // Then
    assertResponseStatus(response, HttpStatus.UNAUTHORIZED);
  }

  @Test
  void userAccessingPrivilegedEndpoint_ShouldReturnForbidden() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);

    // When
    ResponseEntity<String> response = POST(
        request,
        BASE_URL + "/prompts",
        String.class,
        PrivilegeLevel.LOW
    );

    // Then
    assertResponseStatus(response, HttpStatus.FORBIDDEN);
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void createPrompt_Success() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);

    // When
    ResponseEntity<PromptResponse> response = POST(
        request,
        BASE_URL + "/prompts",
        PromptResponse.class,
        PrivilegeLevel.MEDIUM
    );

    // Then
    assertResponseStatus(response, HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(response.getBody().getVersion()).isEqualTo(TEST_PROMPT_VERSION);

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }
}