package com.minionslab.core.service;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelCallServiceTest {
    private ChainRegistry chainRegistry;
    private ModelCallService service;

    @BeforeEach
    void setUp() {
        chainRegistry = mock(ChainRegistry.class);
        service = new ModelCallService(mock(ObjectProvider.class));
    }

    @Test
    void testGetModelNamesAndCacheSize() {
        assertEquals(0, service.getCacheSize());
        assertTrue(service.getModelNames().isEmpty());
    }

    @Test
    void testClearCache() {
        var cache = (java.util.Map<String, ModelInfo>) 
            ReflectionTestUtils.getField(service, "modelInfoCache");
        cache.put("foo", mock(ModelInfo.class));
        assertEquals(1, service.getCacheSize());
        service.clearCache();
        assertEquals(0, service.getCacheSize());
    }

    @Test
    void testCallDelegatesToChainRegistry() {
        ModelCall call = mock(ModelCall.class);
        ModelCall expected = mock(ModelCall.class);
        when(chainRegistry.process(call)).thenReturn(expected);
        assertEquals(expected, service.call(call));
        verify(chainRegistry).process(call);
    }

    @Test
    void testGetModelInfo() {
        ModelInfo info = mock(ModelInfo.class);
        when(info.provider()).thenReturn("openai");
        when(info.modelId()).thenReturn("gpt-4");
        var cache = (java.util.Map<String, ModelInfo>)
            ReflectionTestUtils.getField(service, "modelInfoCache");
        cache.put("openai:gpt-4", info);
        assertEquals(info, service.getModelInfo("openai", "gpt-4"));
        assertNull(service.getModelInfo("other", "gpt-4"));
    }
} 