package com.minionslab.core.common.config;

import com.minionslab.core.domain.BaseEntity;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
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
public class MongoAuditingConfig {

  @Bean
  public AuditorAware<String> auditorProvider(MinionContextHolder contextHolder) {
    return () -> Optional.ofNullable(contextHolder.getContext())
        .map(MinionContext::getUserId);
  }

  @Bean
  public AbstractMongoEventListener<BaseEntity> mongoEventListener(MinionContextHolder contextHolder) {
    return new AbstractMongoEventListener<BaseEntity>() {
      @Override
      public void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {
        super.onBeforeConvert(event);
        BaseEntity entity = event.getSource();
        MinionContext context = contextHolder.getContext();

        // Set internal ID if not set
        if (entity != null) {
          if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
          }
          if (entity.getEntityId() == null ) {
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

        if (context != null) {
          // Set tenant ID only if not already set
          if (entity.getTenantId() == null) {
            entity.setTenantId(context.getTenantId());
          }

          // Set created fields only if they're null (first time)
          if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
            entity.setCreatedBy(context.getUserId());
          }

          // Always update the modified fields
          entity.setUpdatedAt(Instant.now());
          entity.setUpdatedBy(context.getUserId());
        }
      }
    };
  }

  @Bean
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(Instant.now());
  }
} 