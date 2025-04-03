package com.minionslab.core.api;

import static com.minionslab.core.api.BaseControllerIntegrationTest.PrivilegeLevel.HIGH;
import static com.minionslab.core.api.BaseControllerIntegrationTest.PrivilegeLevel.LOW;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_UPDATED_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.MinionResponse;
import com.minionslab.core.api.dto.PromptResponse;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.config.WithMockMinionUser;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
public class ScenarioIntegrationTest extends BaseControllerIntegrationTest {


  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  public void test_create_complete_prompt() throws Exception {
    create_Prompt();
    create_minion();
  }

  private void create_minion() {
    CreateMinionRequest request = context.get(CreateMinionRequest.class);
    ResponseEntity<MinionResponse> response = POST(request, "/api/v1/minions", MinionResponse.class, HIGH);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    log.info(response.getBody().toString());
  }

  private void create_Prompt() {
    CreatePromptRequest request = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> response = POST(request, "/api/v1/prompts", PromptResponse.class, HIGH);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    log.info(response.getBody().toString());
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void createPrompt_Success() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);

    // When
    ResponseEntity<PromptResponse> response = POST(request, "/api/v1/prompts", PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(response.getBody().getVersion()).isEqualTo(TEST_PROMPT_VERSION);

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void getPrompt_Success() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(request, "/api/v1/prompts", PromptResponse.class, LOW);
    String promptId = createResponse.getBody().getId();

    // When
    ResponseEntity<PromptResponse> response = GET("/api/v1/prompts/" + promptId, PromptResponse.class, LOW);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(promptId);
    assertThat(response.getBody().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_Success() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);

    // When
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo(TEST_PROMPT_UPDATED_DESCRIPTION);
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void deletePrompt_Success() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(request, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // When
    ResponseEntity<Void> response = DELETE("/api/v1/prompts/" + promptId, Void.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  /*
  todo: Figure out how to lock a prompt
   */
//  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_WhenLocked_ShouldFail() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // Lock the prompt
    ResponseEntity<Void> lockResponse = POST(null, "/api/v1/prompts/" + promptId + "/lock", Void.class, HIGH);
    assertThat(lockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);

    // When/Then
    assertThatThrownBy(() ->
        PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH))
        .isInstanceOf(HttpClientErrorException.Forbidden.class)
        .hasMessageContaining("Prompt is locked");
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_WhenNonExistent_ShouldFail() {
    // Given
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);
    String nonExistentPromptId = "non-existent-id";

    // When/Then
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + nonExistentPromptId, PromptResponse.class, HIGH);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

  }

  @Test
  @WithMockMinionUser(roles = {"USER"}) // Non-admin user
  void updatePrompt_WhenUnauthorized_ShouldFail() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);

    // When/Then
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, LOW);
    assertThat(
        response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

  }



  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void createPrompt_WithRequiredToolboxes_Success() {
    // Given
    CreatePromptRequest request = context.get(CreatePromptRequest.class);
    request.setMinionType(MinionType.CHAT); // Assuming CHAT type requires specific toolboxes
    request.setToolboxes(Set.of("search", "calculator")); // Required toolboxes for CHAT type

    // When
    ResponseEntity<PromptResponse> response = POST(request, "/api/v1/prompts", PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToolboxes())
        .containsExactlyInAnyOrder("search", "calculator");

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getToolboxes())
        .containsExactlyInAnyOrder("search", "calculator");
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_AddToolboxes_Success() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // Create update request to add toolboxes
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);
    updateRequest.setToolboxes(Set.of("search", "calculator"));

    // When
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToolboxes())
        .containsExactlyInAnyOrder("search", "calculator");

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getToolboxes())
        .containsExactlyInAnyOrder("search", "calculator");
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_RemoveToolboxes_Success() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    createRequest.setToolboxes(Set.of("search", "calculator"));
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // Create update request to remove toolboxes
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);
    updateRequest.setToolboxes(Set.of()); // Empty set to remove all toolboxes

    // When
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToolboxes()).isEmpty();

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getToolboxes()).isEmpty();
  }

  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_UpdateToolboxes_Success() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    createRequest.setToolboxes(Set.of("search", "calculator"));
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // Create update request to change toolboxes
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);
    updateRequest.setToolboxes(Set.of("search", "translator")); // Replace calculator with translator

    // When
    ResponseEntity<PromptResponse> response = PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToolboxes())
        .containsExactlyInAnyOrder("search", "translator");

    // Verify in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(response.getBody().getId());
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getToolboxes())
        .containsExactlyInAnyOrder("search", "translator");
  }

  /*
  todo: We need a way to lock a prompt and then run this test. Currently there is no way to lock a prompt externally.
   */
//  @Test
  @WithMockMinionUser(roles = {"ADMIN"})
  void updatePrompt_WhenLocked_ShouldNotUpdateToolboxes() {
    // Given
    CreatePromptRequest createRequest = context.get(CreatePromptRequest.class);
    createRequest.setToolboxes(Set.of("search", "calculator"));
    ResponseEntity<PromptResponse> createResponse = POST(createRequest, "/api/v1/prompts", PromptResponse.class, HIGH);
    String promptId = createResponse.getBody().getId();

    // Lock the prompt
    ResponseEntity<Void> lockResponse = POST(null, "/api/v1/prompts/" + promptId + "/lock", Void.class, HIGH);
    assertThat(lockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Create update request to change toolboxes
    UpdatePromptRequest updateRequest = context.get(UpdatePromptRequest.class);
    updateRequest.setToolboxes(Set.of("search", "translator"));

    // When/Then
    assertThatThrownBy(() ->
        PUT(updateRequest, "/api/v1/prompts/" + promptId, PromptResponse.class, HIGH))
        .isInstanceOf(HttpClientErrorException.Forbidden.class)
        .hasMessageContaining("Prompt is locked");

    // Verify toolboxes haven't changed in database
    Optional<MinionPrompt> savedPrompt = promptRepository.findById(promptId);
    assertThat(savedPrompt).isPresent();
    assertThat(savedPrompt.get().getToolboxes())
        .containsExactlyInAnyOrder("search", "calculator");
  }
}
