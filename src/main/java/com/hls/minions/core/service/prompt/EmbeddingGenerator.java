package com.hls.minions.core.service.prompt;

import java.util.List;


public class EmbeddingGenerator {
  private final OpenAIClient openAIClient;

  public EmbeddingGenerator(OpenAIClient client) {
    this.openAIClient = client;
  }

  public List<Float> generateEmbedding(String text) {
    return openAIClient.createEmbedding(text);
  }
}

