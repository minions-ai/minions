package com.hls.minions.core.agent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import javax.naming.OperationNotSupportedException;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;

public class MongoDBPromptLoader implements IPromptLoader {

  @Override public String getExactPrompt(String scopeKey) {
    return "";
  }

  @Override public String getBestMatchingPrompt(String taskDescription) throws OperationNotSupportedException {
    return "";
  }
/*
  private final MongoCollection<Document> collection;
  private final EmbeddingGenerator embeddingGenerator;

  public MongoDBPromptLoader(EmbeddingGenerator embeddingGenerator) {
    MongoClient mongoClient = MongoClients.create(ConfigLoader.getProperty("mongodb.uri"));
    MongoDatabase database = mongoClient.getDatabase(ConfigLoader.getProperty("mongodb.database"));
    this.collection = database.getCollection(ConfigLoader.getProperty("mongodb.collection"));
    this.embeddingGenerator = embeddingGenerator;
  }

  @Override
  // 1️⃣ Load from MongoDB (Exact NoSQL Lookup)
  public String getExactPrompt(String scopeKey) {
    Document query = new Document("scopeKey", scopeKey);
    Document result = collection.find(query).first();
    return result != null ? result.getString("prompt_text") : null;
  }

  // 2️⃣ Load from MongoDB (Vector Search)
  @Override public String getBestMatchingPrompt(String taskDescription) {
    List<Float> queryVector = embeddingGenerator.generateEmbedding(taskDescription);

    Bson query = Filters.near("embedding", queryVector);
    Document result = collection.find(query).sort(Sorts.descending("similarity")).first();

    return result != null ? result.getString("prompt_text") : null;
  }*/
}

