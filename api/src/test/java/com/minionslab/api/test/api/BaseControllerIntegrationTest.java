package com.minionslab.api.test.api;

import static com.minionslab.api.test.api.BaseControllerIntegrationTest.PrivilegeLevel.HIGH;
import static com.minionslab.api.test.api.BaseControllerIntegrationTest.PrivilegeLevel.LOW;
import static com.minionslab.api.test.api.BaseControllerIntegrationTest.PrivilegeLevel.MEDIUM;
import static com.minionslab.api.test.api.BaseControllerIntegrationTest.PrivilegeLevel.NO;
import static com.minionslab.api.test.util.TestConstants.CONTEXT_TEXT;
import static com.minionslab.api.test.util.TestConstants.GUIDELINES_TEXT;
import static com.minionslab.api.test.util.TestConstants.POLICY_TEXT;
import static com.minionslab.api.test.util.TestConstants.REFLECTION_TEXT;
import static com.minionslab.api.test.util.TestConstants.TASK_SPECIFIC_TEXT;
import static com.minionslab.api.test.util.TestConstants.TEST_COMPONENT_ORDER;
import static com.minionslab.api.test.util.TestConstants.TEST_COMPONENT_TEXT;
import static com.minionslab.api.test.util.TestConstants.TEST_COMPONENT_TYPE;
import static com.minionslab.api.test.util.TestConstants.TEST_COMPONENT_WEIGHT;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_CONTENT;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_TYPE;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_UPDATED_DESCRIPTION;
import static com.minionslab.api.test.util.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.api.test.util.TestConstants.TEST_TENANT_ID;
import static com.minionslab.api.test.util.TestConstants.USER_TEMPLATE_TEXT;
import static com.minionslab.api.test.util.TestConstants.getPromptComponent;
import static org.assertj.core.api.Assertions.assertThat;

import com.minionslab.api.test.config.TestSecurityConfig;
import com.minionslab.api.test.controller.dto.CreateMinionRequest;
import com.minionslab.api.test.controller.dto.CreatePromptRequest;
import com.minionslab.api.test.controller.dto.PromptComponentRequest;
import com.minionslab.api.test.controller.dto.UpdatePromptRequest;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.PromptRepository;
import java.time.Instant;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


/**
 * Base class for controller integration tests. Provides common functionality for: - Security testing with different privilege levels - HTTP
 * request/response handling - Test data management - Database management - Test context management
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Testcontainers
public abstract class BaseControllerIntegrationTest {

  protected static final String BASE_URL = "/api/v1";
  protected static final String AUTH_HEADER = "Authorization";

  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.6"));
  @Autowired protected PromptRepository promptRepository;
  @Autowired protected MongoTemplate mongoTemplate;
  protected TestRestTemplate highPrivilegeTemplate;
  protected TestRestTemplate lowPrivilegeTemplate;
  protected TestRestTemplate noPrivilegeTemplate;
  @LocalServerPort protected int port;
  protected HttpHeaders headers;
  protected TestContext context;
  private Map<PrivilegeLevel, TestRestTemplate> restTemplates = new HashMap<>();

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    log.info("Using TestContainer MongoDB");
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @BeforeEach
  protected void setUp() {
    cleanDatabase();
    setupRestTemplates();
    initHttp();
    context = createTestContext();
  }

  @AfterEach
  protected void tearDown() {
    cleanDatabase();
  }

  protected String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }

  private void initHttp() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  protected TestContext createTestContext() {
    TestContext context = new TestContext();

    // Create test messages
    MinionPrompt testPrompt = MinionPrompt.builder()
        .description(TEST_PROMPT_DESCRIPTION)
        .tenantId(TEST_TENANT_ID)
        .version(TEST_PROMPT_VERSION)
        .components(new HashMap<>(
            Map.of(TEST_PROMPT_TYPE, getPromptComponent(PromptType.DYNAMIC, TEST_PROMPT_CONTENT))))
        .build();
    context.put(MinionPrompt.class, testPrompt);

    // Create messages components
    List<PromptComponent> promptComponents = List.of(
        PromptComponent.builder().type(PromptType.GUIDELINES).text(GUIDELINES_TEXT).build(),
        PromptComponent.builder().type(PromptType.CONTEXT).text(CONTEXT_TEXT).build(),
        PromptComponent.builder().type(PromptType.SYSTEM).text(REFLECTION_TEXT).build(),
        PromptComponent.builder().type(PromptType.POLICY).text(POLICY_TEXT).build(),
        PromptComponent.builder().type(PromptType.TASK_SPECIFIC).text(TASK_SPECIFIC_TEXT).build(),
        PromptComponent.builder().type(PromptType.USER_TEMPLATE).text(USER_TEMPLATE_TEXT).build()

    );
    context.put(List.class, promptComponents);

    // Create request
    CreatePromptRequest createRequest = CreatePromptRequest.builder()
        .entityId(TEST_PROMPT_ENTITY_ID)
        .description(TEST_PROMPT_DESCRIPTION)
        .metadata(Map.of("modelId", "test"))
        .build();

    for (PromptComponent promptComponent : promptComponents) {
      PromptComponentRequest promptComponentRequest = PromptComponentRequest.builder()
          .type(promptComponent.getType())
          .content(promptComponent.getText())
          .build();
      createRequest.getComponents().add(promptComponentRequest);
    }


    context.put(CreatePromptRequest.class, createRequest);

    // Create update request
    UpdatePromptRequest updateRequest = UpdatePromptRequest.builder()
        .description(TEST_PROMPT_UPDATED_DESCRIPTION)
        .build();

    context.put(UpdatePromptRequest.class, updateRequest);

    // Create component request
    PromptComponentRequest componentRequest = PromptComponentRequest.builder()
        .content(TEST_COMPONENT_TEXT)
        .type(TEST_COMPONENT_TYPE)
        .weight(TEST_COMPONENT_WEIGHT)
        .order(TEST_COMPONENT_ORDER)
        .build();
    context.put(PromptComponentRequest.class, componentRequest);

    CreateMinionRequest createMinionRequest = CreateMinionRequest.builder()
        .effectiveDate(Instant.now().plus(Period.of(0, 0, 2)))
        .minionType(MinionType.USER_DEFINED_AGENT)
        .promptEntityId(TEST_PROMPT_ENTITY_ID)
        .build();
    context.put(CreateMinionRequest.class, createMinionRequest);

    return context;
  }

  private void setupRestTemplates() {
    restTemplates.put(HIGH, new TestRestTemplate("admin", "password", HttpClientOption.ENABLE_COOKIES));
    restTemplates.put(MEDIUM, new TestRestTemplate("user", "password", HttpClientOption.ENABLE_COOKIES));
    restTemplates.put(LOW, new TestRestTemplate("user2", "password", HttpClientOption.ENABLE_COOKIES));
    restTemplates.put(NO, new TestRestTemplate());

  }

  /**
   * Generic HTTP request method that can handle different request and response types
   */
  protected <T, R> ResponseEntity<R> exchange(T requestBody, String path, HttpMethod method, Class<R> responseType,
      PrivilegeLevel privilegeLevel) {
    try {
      HttpEntity<T> entity = createHttpEntity(requestBody);
      TestRestTemplate template = getTemplateForPrivilegeLevel(privilegeLevel);
      return template.exchange(
          createURLWithPort(path),
          method,
          entity,
          responseType
      );
    } catch (Exception e) {
      log.error("Error during HTTP exchange: {}", e.getMessage());
      throw e;
    }
  }

  private TestRestTemplate getTemplateForPrivilegeLevel(PrivilegeLevel level) {
    return restTemplates.getOrDefault(level, restTemplates.get(NO));
  }


  /**
   * HTTP GET request with privilege level
   */
  protected <R> ResponseEntity<R> GET(String path, Class<R> responseType, PrivilegeLevel privilegeLevel) {
    return exchange(null, path, HttpMethod.GET, responseType, privilegeLevel);
  }

  /**
   * HTTP POST request with privilege level
   */
  protected <T, R> ResponseEntity<R> POST(T requestBody, String path, Class<R> responseType, PrivilegeLevel privilegeLevel) {
    return exchange(requestBody, path, HttpMethod.POST, responseType, privilegeLevel);
  }

  /**
   * HTTP PUT request with privilege level
   */
  protected <T, R> ResponseEntity<R> PUT(T requestBody, String path, Class<R> responseType, PrivilegeLevel privilegeLevel) {
    return exchange(requestBody, path, HttpMethod.PUT, responseType, privilegeLevel);
  }

  /**
   * HTTP DELETE request with privilege level
   */
  protected <R> ResponseEntity<R> DELETE(String path, Class<R> responseType, PrivilegeLevel privilegeLevel) {
    return exchange(null, path, HttpMethod.DELETE, responseType, privilegeLevel);
  }

  protected <T> HttpEntity<T> createHttpEntity(T body) {
    return new HttpEntity<>(body, headers);
  }

  protected void cleanDatabase() {
    mongoTemplate.getDb().listCollectionNames().forEach(collectionName ->
        mongoTemplate.getDb().getCollection(collectionName).drop());
  }

  /**
   * Assert response status code
   */
  protected <T> void assertResponseStatus(ResponseEntity<T> response, HttpStatus expectedStatus) {
    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
  }

  /**
   * Assert response body
   */
  protected <T> void assertResponseBody(ResponseEntity<T> response, T expectedBody) {
    assertThat(response.getBody()).isEqualTo(expectedBody);
  }

  /**
   * Create HTTP entity with authentication headers
   */
  protected <T> HttpEntity<T> createHttpEntityWithAuth(T body, PrivilegeLevel level) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  /**
   * Privilege levels for security testing
   */
  public enum PrivilegeLevel {
    HIGH, MEDIUM,LOW, NO
  }

  /**
   * Context class to hold test-specific entities
   */
  protected static class TestContext {

    private final Map<Class<?>, Object> entities = new HashMap<>();

    public <T> T get(Class<T> type) {
      return (T) entities.get(type);
    }

    public <T> void put(Class<T> type, T entity) {
      entities.put(type, entity);
    }
  }

}
