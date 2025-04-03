package com.minionslab.core.domain;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document
public abstract class BaseEntity implements TenantAware {

  @Id
  @Default
  protected String id = UUID.randomUUID().toString();
  @NotNull protected String entityId;  // Business identifier that remains constant across versions

  @Version
  private Long dbVersion;
  
  @CreatedDate
  @Field("created_at")
  private Instant createdAt;
  @LastModifiedDate
  @Field("updated_at")
  private Instant updatedAt;
  @CreatedBy
  @Field("created_by")
  private String createdBy;
  @LastModifiedBy
  @Field("updated_by")
  private String updatedBy;
  @Field("tenant_id")
  private String tenantId;
}