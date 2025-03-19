package com.minionslab.core.domain;


import java.time.Instant;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuperBuilder
public abstract class BaseEntity implements TenantAware {

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