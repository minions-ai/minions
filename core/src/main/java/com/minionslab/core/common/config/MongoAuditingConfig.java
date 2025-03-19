package com.minionslab.core.common.config;

import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.BaseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    MinionContext minionContext = MinionContextHolder.getContext();
    return () -> java.util.Optional.of(minionContext.getUserId());
  }

  @Bean
  public AbstractMongoEventListener<BaseEntity> mongoEventListener() {
    MinionContext minionContext = MinionContextHolder.getContext();
    return new AbstractMongoEventListener<BaseEntity>() {
      @Override
      public void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {

        super.onBeforeConvert(event);
        BaseEntity entity = event.getSource();
        entity.setTenantId(minionContext.getTenantId());
      }

      @Override
      public void onBeforeSave(BeforeSaveEvent<BaseEntity> event) {
        super.onBeforeSave(event);
        BaseEntity entity = event.getSource();
        entity.setTenantId(minionContext.getTenantId());
      }
    };
  }
} 