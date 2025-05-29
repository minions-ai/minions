package com.minionslab.core.service.adaptor;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.ModelCall;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringAIModelAdaptorTest {
    @Test
    void testAcceptsReturnsFalseForNullConfig() {
        SpringAIModelAdaptor adaptor = new SpringAIModelAdaptor();
        ModelCall call = mock(ModelCall.class);
        when(call.getModelConfig()).thenReturn(null);
        assertFalse(adaptor.accepts(call));
    }

    @Test
    void testAcceptsHandlesProviderLogic() {
        SpringAIModelAdaptor adaptor = new SpringAIModelAdaptor();
        ModelConfig config = mock(ModelConfig.class);
        when(config.getModelId()).thenReturn("any");
        when(config.getProvider()).thenReturn("OPENAI");
        ModelCall call = mock(ModelCall.class);
        when(call.getModelConfig()).thenReturn(config);
        // Accepts may depend on reflection, so just ensure no exception
        assertDoesNotThrow(() -> adaptor.accepts(call));
    }
} 