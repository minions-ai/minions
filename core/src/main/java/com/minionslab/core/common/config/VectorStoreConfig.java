package com.minionslab.core.common.config;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore.SimpleVectorStoreBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

  @Value("spring.ai.openai.api-key")
  private String apiKey;

  @Bean
  public VectorStore pineconeVectorStore() {
    SimpleVectorStoreBuilder builder = SimpleVectorStore.builder(new OpenAiEmbeddingModel(new OpenAiApi(apiKey)));
    return builder.build();
  }

}
