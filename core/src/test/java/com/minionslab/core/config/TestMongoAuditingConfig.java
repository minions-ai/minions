package com.minionslab.core.config;

import static com.minionslab.core.util.TestConstants.TEST_TENANT_ID;
import static com.minionslab.core.util.TestConstants.TEST_USER_ID;

import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.BaseEntity;
import com.minionslab.core.domain.MinionPrompt;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

@Configuration
@EnableMongoAuditing(
    auditorAwareRef = "auditorProvider",
    dateTimeProviderRef = "dateTimeProvider",
    setDates = true,
    modifyOnCreate = true
)
public class TestMongoAuditingConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(MinionContextHolder.getContext())
        .map(MinionContext::getUserId)
        .or(() -> Optional.of("test-user"));
  }

  @Bean
  public AbstractMongoEventListener<BaseEntity> mongoEventListener() {
    return new AbstractMongoEventListener<BaseEntity>() {
      @Override
      public void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {
        super.onBeforeConvert(event);
        BaseEntity entity = event.getSource();

        // Set internal ID if not set
        if (entity != null) {
          if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
          }
          if (entity.getEntityId() == null) {
            entity.setEntityId(entity.getId());
          }

          // Handle MinionPrompt specific logic
          if (entity instanceof MinionPrompt) {
            MinionPrompt prompt = (MinionPrompt) entity;
            // Set entityId only for new prompts (not for new versions)
            if (prompt.getEntityId() == null) {
              prompt.setEntityId(UUID.randomUUID().toString());
            }
            if (prompt.getVersion() == null) {
              prompt.setVersion("1.0");
            }
          }
        }

        // Set tenant ID only if not already set
        if (entity.getTenantId() == null) {
          entity.setTenantId(TEST_TENANT_ID);

          // Set created fields only if they're null (first time)
          if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
            entity.setCreatedBy(TEST_USER_ID);
          }

          // Always update the modified fields
          entity.setUpdatedAt(Instant.now());
          entity.setUpdatedBy(TEST_USER_ID);
        }
      }
    };
  }

  @Bean
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(Instant.now());
  }
} 