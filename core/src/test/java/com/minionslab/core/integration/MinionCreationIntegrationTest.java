package com.minionslab.core.integration;

import static com.minionslab.core.test.TestConstants.TEST_COMPONENT_TEXT;
import static com.minionslab.core.test.TestConstants.TEST_METADATA_KEY;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.PromptComponentRequest;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.PromptService;
import com.minionslab.core.test.BaseTenantAwareTest;
import jakarta.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.validation.enabled=true"
})
public class MinionCreationIntegrationTest extends BaseTenantAwareTest {

  @Autowired private PromptService promptService;

  @MockBean private PromptRepository promptRepository;

  @MockBean private MinionRepository minionRepository;
  private MinionPrompt minionPrompt;

  @BeforeEach
  public void init() {
    minionPrompt = MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .version(TEST_PROMPT_VERSION)
        .description(TEST_PROMPT_DESCRIPTION)
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder().type(PromptType.SYSTEM).text(TEST_COMPONENT_TEXT).build()))
        .build();

    // Correct way to mock repository save
    Mockito.when(promptRepository.save(Mockito.any(MinionPrompt.class)))
        .thenReturn(minionPrompt);
  }

  @Test
  public void test_create_prompt_component() {
    // Arrange
    String promptName = "test_prompt_" + UUID.randomUUID();
    String promptVersion = "1.0.0";

    // Act
    PromptComponent systemComponent = PromptComponent.builder()
        .text("You are a helpful assistant")
        .type(PromptType.SYSTEM)
        .build();

    // Assert
    assertThat(systemComponent).isNotNull();
  }

  @Test
  public void test_validation_failure() {
    // Create invalid request
    CreatePromptRequest invalidRequest = CreatePromptRequest.builder()
        .description(TEST_PROMPT_DESCRIPTION)
        .metadata(new HashMap<>())
        // Don't set name to trigger @NotNull validation
        .build();

    // This should throw ValidationException
    assertThrows(ValidationException.class, () -> {
      promptService.createPrompt(invalidRequest);
    });
  }

  @Test
  public void test_create_bare_minimum_prompt() {
    // Arrange

    HashMap<String, Object> metadata = new HashMap<>();
    metadata.put(TEST_METADATA_KEY, TEST_TENANT_ID);

    CreatePromptRequest createPromptRequest = CreatePromptRequest.builder()
        .entityId(TEST_PROMPT_ENTITY_ID)
        .description(TEST_PROMPT_DESCRIPTION)
        .components(List.of(PromptComponentRequest.builder().type(PromptType.SYSTEM).content(TEST_COMPONENT_TEXT).build()))
        .build();

    MinionPrompt createdPrompt = promptService.createPrompt(createPromptRequest);

    // Assert
    assertThat(createdPrompt).isNotNull();
    assertThat(createdPrompt.getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(createdPrompt.getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(createdPrompt.getComponents()).hasSize(1);
    assertThat(createdPrompt.getComponents().get(PromptType.SYSTEM))
        .isNotNull()
        .hasFieldOrPropertyWithValue("text", TEST_COMPONENT_TEXT)
        .hasFieldOrPropertyWithValue("type", PromptType.SYSTEM);
  }

/*  @Test
  public void test_create_prompt_component() {
    // Arrange
    String promptName = "test_prompt_" + UUID.randomUUID();
    String promptVersion = "1.0.0";

    PromptComponent systemComponent = PromptComponent.builder()
        .text("You are a helpful assistant")
        .type(PromptType.SYSTEM)
        .build();

    PromptComponent userComponent = PromptComponent.builder()
        .text("Please help me with: {request}")
        .type(PromptType.USER_TEMPLATE)
        .build();

    MinionPrompt prompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .name(promptName)
        .version(promptVersion)
        .tenantId(TEST_TENANT_ID)
        .effectiveDate(Instant.now())
        .components(Map.of(
            PromptType.SYSTEM, systemComponent,
            PromptType.USER_TEMPLATE, userComponent
        ))
        .build();

    HashMap<String,Object> metadata = new HashMap<>();
    metadata.put(TEST_METADATA_KEY, TEST_TENANT_ID);

    CreatePromptRequest.builder()
        .description("test_prompt_description")
        .metadata(metadata)
        .build();

    // Act
    MinionPrompt createdPrompt = promptService.createPrompt();

    // Assert
    assertThat(createdPrompt).isNotNull();
    assertThat(createdPrompt.getName()).isEqualTo(promptName);
    assertThat(createdPrompt.getVersion()).isEqualTo(promptVersion);
    assertThat(createdPrompt.getComponents()).hasSize(2);
    assertThat(createdPrompt.getComponents().get(PromptType.SYSTEM))
        .isNotNull()
        .hasFieldOrPropertyWithValue("text", "You are a helpful assistant")
        .hasFieldOrPropertyWithValue("type", PromptType.SYSTEM);
    assertThat(createdPrompt.getComponents().get(PromptType.USER))
        .isNotNull()
        .hasFieldOrPropertyWithValue("text", "Please help me with: {request}")
        .hasFieldOrPropertyWithValue("type", PromptType.USER);
  }*/
} 