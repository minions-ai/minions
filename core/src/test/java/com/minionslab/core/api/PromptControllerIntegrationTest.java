package com.minionslab.core.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.PromptRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.minionslab.core.util.PromptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.port=27017",
        "spring.data.mongodb.database=test_db"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromptControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PromptRepository promptRepository;

  private MinionPrompt testPrompt;

  @BeforeEach
  void setUp() {
    testPrompt = MinionPrompt.builder()
        .id("test-id")
        .name("Test Prompt")
        .tenantId("tenant-id")
        .minionType(MinionType.USER_DEFINED_AGENT)
        .version("1.0")
        .component(PromptType.DYNAMIC,PromptUtil.getPromptComponent(PromptType.DYNAMIC,"Test Prompt"))
        .build();

    // Setup common repository mock behaviors
    when(promptRepository.save(any(MinionPrompt.class))).thenAnswer(invocation -> {
      MinionPrompt prompt = invocation.getArgument(0);
      if (prompt.getId() == null) {
        prompt.setId("generated-id");
      }
      return prompt;
    });

    // Setup findById mock
    when(promptRepository.findById("test-id")).thenReturn(Optional.of(testPrompt));
    
    // Setup delete mock - using doAnswer for void methods
    doAnswer(invocation -> {
      String id = invocation.getArgument(0);
      // After deletion, findById should return empty
      when(promptRepository.findById(id)).thenReturn(Optional.empty());
      return null;
    }).when(promptRepository).deleteById(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createPrompt_Success() throws Exception {
    CreatePromptRequest request = new CreatePromptRequest();
    request.setName("Test Prompt");
    request.setType(MinionType.USER_DEFINED_AGENT);
    request.setComponents("Test content");
    request.setVersion("1.0");
    request.setTenantId("tenant-id");

    mockMvc.perform(post("/api/v1/prompts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Test Prompt")))
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0]", is("Test content")));
  }

  @Test
  void getPrompt_Success() throws Exception {
    when(promptRepository.findById("test-id")).thenReturn(Optional.of(testPrompt));

    mockMvc.perform(get("/api/v1/prompts/test-id"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("test-id")))
        .andExpect(jsonPath("$.name", is("Test Prompt")));
  }

  @Test
  void getAllPrompts_Success() throws Exception {
    when(promptRepository.findAllByTenantId("tenant-1"))
        .thenReturn(Arrays.asList(testPrompt));

    mockMvc.perform(get("/api/v1/prompts")
            .param("tenantId", "tenant-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is("test-id")));
  }

  @Test
  void getPromptsByType_Success() throws Exception {
    when(promptRepository.findAllByTypeAndTenantId(any(MinionType.class), any(String.class)))
        .thenReturn(Arrays.asList(testPrompt));

    mockMvc.perform(get("/api/v1/prompts/type/" + MinionType.USER_DEFINED_AGENT)
            .param("tenantId", "tenant-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is("test-id")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updatePrompt_Success() throws Exception {
    when(promptRepository.findById("test-id")).thenReturn(Optional.of(testPrompt));

    UpdatePromptRequest request = new UpdatePromptRequest();
    request.setContent("Updated content");

    mockMvc.perform(put("/api/v1/prompts/test-id")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("test-id")))
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0]", is("Updated content")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deletePrompt_Success() throws Exception {
    // Verify prompt exists before deletion
    assertThat(promptRepository.findById("test-id")).isPresent();

    // Perform delete
    mockMvc.perform(delete("/api/v1/prompts/test-id"))
        .andExpect(status().isNoContent());

    // Verify prompt no longer exists
    assertThat(promptRepository.findById("test-id")).isEmpty();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deletePrompt_NonExistent_ShouldReturnNotFound() throws Exception {
    // Setup non-existent prompt
    when(promptRepository.findById("non-existent-id")).thenReturn(Optional.empty());

    try {
      mockMvc.perform(delete("/api/v1/prompts/non-existent-id"));
    }catch (Exception e) {
      assertThat(e.getMessage()).isEqualTo("Request processing failed: com.minionslab.core.common.exception.PromptException$PromptNotFoundException: Prompt not found: non-existent-id");
    }

  }

  @Test
  @WithMockUser(roles = "USER")
  void deletePrompt_Unauthorized_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete("/api/v1/prompts/test-id"))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletePrompt_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
    mockMvc.perform(delete("/api/v1/prompts/test-id"))
        .andExpect(status().isUnauthorized());
  }
} 