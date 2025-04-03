package com.minionslab.core.domain.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.annotation.Toolbox;
import com.minionslab.core.domain.tools.exception.ToolGroupException;
import com.minionslab.core.domain.tools.exception.ToolInitializationException;
import com.minionslab.core.domain.tools.exception.ToolNotFoundException;
import com.minionslab.core.domain.tools.exception.ToolRegistrationException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ToolRegistryTest {

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private ObjectMapper objectMapper;

  private ToolRegistry toolRegistry;

  @BeforeEach
  void setUp() {
    toolRegistry = new ToolRegistry();
    ReflectionTestUtils.setField(toolRegistry, "applicationContext", applicationContext);
    ReflectionTestUtils.setField(toolRegistry, "objectMapper", objectMapper);
  }

  @Test
  void testSetApplicationContext() {
    assertDoesNotThrow(() -> toolRegistry.setApplicationContext(applicationContext));
  }

  @Test
  void testSetApplicationContextWithNull() {
    assertThrows(ToolInitializationException.class, () -> toolRegistry.setApplicationContext(null));
  }

  @Test
  void testCreateToolGroup() {
    assertDoesNotThrow(() -> toolRegistry.createToolGroup("testGroup", "Test Description", "tool1", "tool2"));
  }

  @Test
  void testCreateToolGroupWithNullId() {
    assertThrows(ToolGroupException.class, () ->
        toolRegistry.createToolGroup(null, "Test Description", "tool1"));
  }

  @Test
  void testCreateToolGroupWithEmptyId() {
    assertThrows(ToolGroupException.class, () ->
        toolRegistry.createToolGroup("", "Test Description", "tool1"));
  }

  @Test
  void testCreateToolGroupWithNullTools() {
    assertThrows(ToolGroupException.class, () ->
        toolRegistry.createToolGroup("testGroup", "Test Description", (String[]) null));
  }

  @Test
  void testCreateToolGroupWithEmptyTools() {
    assertThrows(ToolGroupException.class, () ->
        toolRegistry.createToolGroup("testGroup", "Test Description"));
  }

  @Test
  void testRegisterToolbox() {
    // Create a mock bean with @Toolbox annotation
    @Toolbox(name = "testTool", version = "1.0", categories = {"test"})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    toolRegistry.discoverToolboxes();

    // Verify toolbox was registered
    Object retrievedTool = toolRegistry.getToolbox("testTool");
    assertNotNull(retrievedTool);
    assertTrue(retrievedTool instanceof TestTool);
  }

  @Test
  void testRegisterToolboxWithNullName() {
    @Toolbox(name = "", version = "1.0", categories = {"test"})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    assertThrows(ToolRegistrationException.class, () -> toolRegistry.discoverToolboxes());
  }

  @Test
  void testGetToolboxWithVersion() {
    // Create a mock bean with @Toolbox annotation
    @Toolbox(name = "testTool", version = "1.0", categories = {"test"})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    toolRegistry.discoverToolboxes();

    // Test getting specific version
    Object retrievedTool = toolRegistry.getToolbox("testTool", "1.0");
    assertNotNull(retrievedTool);
    assertTrue(retrievedTool instanceof TestTool);
  }

  @Test
  void testGetToolboxWithNonExistentVersion() {
    // Create a mock bean with @Toolbox annotation
    @Toolbox(name = "testTool", version = "1.0", categories = {"test"})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    toolRegistry.discoverToolboxes();

    assertThrows(ToolNotFoundException.class, () ->
        toolRegistry.getToolbox("testTool", "2.0"));
  }

  @Test
  void testGetToolboxWithNonExistentTool() {
    assertThrows(ToolNotFoundException.class, () ->
        toolRegistry.getToolbox("nonExistentTool"));
  }

  @Test
  void testGetToolboxWithNullName() {
    assertThrows(ToolNotFoundException.class, () ->
        toolRegistry.getToolbox(null));
  }

  @Test
  void testGetToolboxWithEmptyName() {
    assertThrows(ToolNotFoundException.class, () ->
        toolRegistry.getToolbox(""));
  }

  @Test
  void testMultipleVersions() {
    // Create two versions of the same tool
    @Toolbox(name = "testTool", version = "1.0", categories = {"test"})
    class TestToolV1 {

    }

    @Toolbox(name = "testTool", version = "2.0", categories = {"test"})
    class TestToolV2 {

    }

    TestToolV1 v1 = new TestToolV1();
    TestToolV2 v2 = new TestToolV2();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testToolV1", v1, "testToolV2", v2));

    toolRegistry.discoverToolboxes();

    // Test getting specific versions
    Object retrievedV1 = toolRegistry.getToolbox("testTool", "1.0");
    Object retrievedV2 = toolRegistry.getToolbox("testTool", "2.0");
    Object retrievedLatest = toolRegistry.getToolbox("testTool");

    assertNotNull(retrievedV1);
    assertNotNull(retrievedV2);
    assertNotNull(retrievedLatest);
    assertTrue(retrievedV1 instanceof TestToolV1);
    assertTrue(retrievedV2 instanceof TestToolV2);
    assertTrue(retrievedLatest instanceof TestToolV2); // Should get latest version
  }

  @Test
  void testToolCategories() {
    @Toolbox(name = "testTool", version = "1.0", categories = {"category1", "category2"})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    toolRegistry.discoverToolboxes();

    // Verify tool is registered in both categories
    Object retrievedTool = toolRegistry.getToolbox("testTool");
    assertNotNull(retrievedTool);
    assertTrue(retrievedTool instanceof TestTool);
  }

  @Test
  void testEmptyCategories() {
    @Toolbox(name = "testTool", version = "1.0", categories = {})
    class TestTool {

    }

    TestTool testTool = new TestTool();
    when(applicationContext.getBeansWithAnnotation(Toolbox.class))
        .thenReturn(Map.of("testTool", testTool));

    toolRegistry.discoverToolboxes();

    // Should still work with empty categories
    Object retrievedTool = toolRegistry.getToolbox("testTool");
    assertNotNull(retrievedTool);
    assertTrue(retrievedTool instanceof TestTool);
  }
} 