package com.minionslab.core.test;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.TenantAware;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTenantAwareTest {

  protected static MockedStatic<MinionContextHolder> mockedStatic = mockStatic(MinionContextHolder.class);
  @Mock(strictness = Strictness.LENIENT)
  protected MinionContext minionContext;

  @BeforeAll
  static void init() {
    // Initialize any static resources if needed
  }

  @AfterAll
  static void destroy() {
    /*if (mockedStatic != null) {
      mockedStatic.close();
    }*/
  }

  @BeforeEach
  void setUpContext() {
    mockedStatic.when(MinionContextHolder::getContext).thenReturn(minionContext);
    when(minionContext.getTenantId()).thenReturn(TestConstants.TEST_TENANT_ID);
    when(minionContext.getUserId()).thenReturn(TestConstants.TEST_USER_ID);
    mockedStatic.when(MinionContextHolder::getContext).thenReturn(minionContext);
    setupDefaultContext();
  }

  protected void setupDefaultContext() {
    when(minionContext.getTenantId()).thenReturn(TestConstants.TEST_TENANT_ID);
    when(minionContext.getUserId()).thenReturn(TestConstants.TEST_USER_ID);
  }

  protected void setTenantId(String tenantId) {
    when(minionContext.getTenantId()).thenReturn(tenantId);
  }

  protected void setUserId(String userId) {
    when(minionContext.getUserId()).thenReturn(userId);
  }

  protected <T> T createTestEntity(Class<T> entityClass) {
    try {
      T entity = entityClass.getDeclaredConstructor().newInstance();
      // Set common fields if entity implements certain interface
      if (entity instanceof TenantAware) {
        ((TenantAware) entity).setTenantId(TestConstants.TEST_TENANT_ID);
      }
      return entity;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create test entity", e);
    }
  }
} 