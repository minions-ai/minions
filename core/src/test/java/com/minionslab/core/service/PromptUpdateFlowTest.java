package com.minionslab.core.service;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.impl.llm.LLMServiceFactory;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ExtendWith(MockitoExtension.class)
class PromptUpdateFlowTest {

  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"));
  @Autowired
  private PromptRepository promptRepository;
  @Autowired
  private MinionRepository minionRepository;
  @Autowired
  private PromptService promptService;
  @Autowired
  private MinionService minionService;
  @Mock
  private LLMServiceFactory llmServiceFactory;
  @Mock
  private LLMService llmService;
  private MinionPrompt initialPrompt;
  private MinionPrompt updatedPrompt;
  private Minion initialMinion;
  private Minion updatedMinion;
  private String testRequest;

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    // Create initial prompt with components
    initialPrompt = MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .description(TEST_PROMPT_DESCRIPTION)
        .version(TEST_PROMPT_VERSION)
        .tenantId(TEST_TENANT_ID)
        .effectiveDate(Instant.now())
        .components(Map.of(
            PromptType.SYSTEM, PromptComponent.builder()
                .type(PromptType.SYSTEM)
                .text("You are a helpful assistant. Always be polite and professional.")
                .build(),
            PromptType.USER_TEMPLATE, PromptComponent.builder()
                .type(PromptType.USER_TEMPLATE)
                .text("Please help me with: {request}")
                .build()
        ))
        .build();

    // Create updated prompt with modified components
    updatedPrompt = MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .description(TEST_PROMPT_DESCRIPTION)
        .version("1.0.1")
        .tenantId(TEST_TENANT_ID)
        .effectiveDate(Instant.now().plusSeconds(1))
        .components(Map.of(
            PromptType.SYSTEM, PromptComponent.builder()
                .type(PromptType.SYSTEM)
                .text("You are a friendly and casual assistant. Feel free to use emojis and be more conversational.")
                .build(),
            PromptType.USER_TEMPLATE, PromptComponent.builder()
                .type(PromptType.USER_TEMPLATE)
                .text("Hey! I need help with: {request}")
                .build()
        ))
        .build();

    // Create test request
    testRequest = "What is the capital of France?";

    LLMResponse response = LLMResponse.builder().responseText("Initial response").build();

    // Setup LLM service mock
    when(llmServiceFactory.getLLMService(any())).thenReturn(llmService);
    when(llmService.processRequest(any())).thenReturn(response);
  }

  @Test
  void testPromptUpdateFlow() {
    // Step 1: Save initial prompt
    initialPrompt = promptRepository.save(initialPrompt);

    // Step 2: Create minion with initial prompt
    initialMinion = Minion.builder()

        .minionPrompt(promptService.getPrompt(initialPrompt.getId()).get())
        .build();
    minionRepository.save(initialMinion);

    // Step 3: Process request with initial prompt
    String initialResponse = minionService.processRequest(initialMinion.getMinionId(), testRequest);
    assertThat(initialResponse).isNotNull();

    // Step 4: Update prompt
    promptService.updatePrompt(TEST_PROMPT_ID, new UpdatePromptRequest(), true);

    // Step 5: Create new minion with updated prompt
    updatedMinion = Minion.builder()

        .minionPrompt(promptService.getPrompt(initialPrompt.getId()).get())
        .build();
    minionRepository.save(updatedMinion);

    // Step 6: Process request with updated prompt
    when(llmService.processRequest(any())).thenReturn(LLMResponse.builder().build());
    String updatedResponse = minionService.processRequest(updatedMinion.getMinionId(), testRequest);
    assertThat(updatedResponse).isNotNull();

    // Verify that the responses are different (due to different prompts)
    assertThat(updatedResponse).isNotEqualTo(initialResponse);

    // Verify that the prompts are different
    MinionPrompt savedInitialPrompt = promptRepository.findByEntityIdAndVersionAndTenantId(
        TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID).orElseThrow();
    MinionPrompt savedUpdatedPrompt = promptRepository.findByEntityIdAndVersionAndTenantId(
        TEST_PROMPT_ENTITY_ID, "1.0.1", TEST_TENANT_ID).orElseThrow();

    assertThat(savedInitialPrompt.getComponents().get(PromptType.SYSTEM).getText())
        .isNotEqualTo(savedUpdatedPrompt.getComponents().get(PromptType.SYSTEM).getText());
    assertThat(savedInitialPrompt.getComponents().get(PromptType.USER_TEMPLATE).getText())
        .isNotEqualTo(savedUpdatedPrompt.getComponents().get(PromptType.USER_TEMPLATE).getText());
  }
} 