package com.minionslab.api.test.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.test.TestConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PromptComponentRequestTest {

    @Configuration
    static class TestConfig {
        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    private LocalValidatorFactoryBean validator;
    private PromptComponentRequest request;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        request = PromptComponentRequest.builder().build();
    }

    @Test
    void toPromptComponent_WithValidData_ShouldCreateComponent() {
        // Arrange
        request.setContent(TestConstants.TEST_PROMPT_CONTENT);
        request.setEmbeddingId(TestConstants.TEST_EMBEDDING_ID);
        request.setWeight(TestConstants.TEST_COMPONENT_WEIGHT);
        request.setOrder(TestConstants.TEST_COMPONENT_ORDER);
        request.setType(PromptType.SYSTEM);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
        request.setMetadatas(metadata);

        // Act
        PromptComponent component = request.toPromptComponent();

        // Assert
        assertThat(component).isNotNull();
        assertThat(component.getText()).isEqualTo(TestConstants.TEST_PROMPT_CONTENT);
        assertThat(component.getEmbeddingId()).isEqualTo(TestConstants.TEST_EMBEDDING_ID);
        assertThat(component.getWeight()).isEqualTo(TestConstants.TEST_COMPONENT_WEIGHT);
        assertThat(component.getOrder()).isEqualTo(TestConstants.TEST_COMPONENT_ORDER);
        assertThat(component.getType()).isEqualTo(PromptType.SYSTEM);
        assertThat(component.getMetadata()).containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
    }

    @Test
    void toPromptComponent_WithDefaultValues_ShouldUseDefaults() {
        // Act
        PromptComponent component = request.toPromptComponent();

        // Assert
        assertThat(component).isNotNull();
        assertThat(component.getWeight()).isEqualTo(1.0);
        assertThat(component.getOrder()).isEqualTo(0.0);
        assertThat(component.getMetadata()).isEmpty();
    }

    @Test
    void validation_WithNullContent_ShouldFail() {
        // Arrange
        request.setType(PromptType.SYSTEM);

        // Act
        var violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Content is required");
    }

    @Test
    void validation_WithNullType_ShouldFail() {
        // Arrange
        request.setContent("Test content");

        // Act
        var violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("PromptType is required");
    }

    @Test
    void validation_WithValidData_ShouldPass() {
        // Arrange
        request.setContent("Test content");
        request.setType(PromptType.SYSTEM);

        // Act
        var violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void builder_WithValidData_ShouldCreateInstance() {
        // Arrange
        String content = "Test content";
        String embeddingId = "embed123";
        double weight = 2.0;
        double order = 1.0;
        PromptType type = PromptType.SYSTEM;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");

        // Act
        PromptComponentRequest request = PromptComponentRequest.builder()
            .content(content)
            .embeddingId(embeddingId)
            .weight(weight)
            .order(order)
            .type(type)
            .metadatas(metadata)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getContent()).isEqualTo(content);
        assertThat(request.getEmbeddingId()).isEqualTo(embeddingId);
        assertThat(request.getWeight()).isEqualTo(weight);
        assertThat(request.getOrder()).isEqualTo(order);
        assertThat(request.getType()).isEqualTo(type);
        assertThat(request.getMetadatas()).containsEntry("key1", "value1");
    }

    @Test
    void builder_WithDefaultValues_ShouldUseDefaults() {
        // Act
        PromptComponentRequest request = PromptComponentRequest.builder()
            .content("Test content")
            .type(PromptType.SYSTEM)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getWeight()).isEqualTo(1.0);
        assertThat(request.getOrder()).isEqualTo(0.0);
        assertThat(request.getMetadatas()).isEmpty();
    }

    @Test
    void builder_WithNullMetadata_ShouldHandleNull() {
        // Act
        PromptComponentRequest request = PromptComponentRequest.builder()
            .content("Test content")
            .type(PromptType.SYSTEM)
            .build();

        // Assert
        assertThat(request.getMetadatas()).isEmpty();
    }

    @Test
    void builder_WithMetadata_ShouldAddMetadata() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);

        // Act
        PromptComponentRequest request = PromptComponentRequest.builder()
            .content("Test content")
            .type(PromptType.SYSTEM)
            .metadatas(metadata)
            .build();

        // Assert
        assertThat(request.getMetadatas())
            .hasSize(1)
            .containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
    }
} 