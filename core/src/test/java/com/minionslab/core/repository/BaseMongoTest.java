package com.minionslab.core.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class BaseMongoTest {


  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.6"));

  protected MongoTemplate mongoTemplate;


  @BeforeEach
  void setUp() {

    // Create MongoClient
    MongoClient mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());

    // Create MongoMappingContext
    MongoMappingContext mappingContext = new MongoMappingContext();
    mappingContext.afterPropertiesSet();

    // Create MappingMongoConverter
    SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, "test");
    MappingMongoConverter converter = new MappingMongoConverter(factory, mappingContext);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null)); // Remove _class
    converter.afterPropertiesSet();

    // Create MongoTemplate
    mongoTemplate = new MongoTemplate(factory, converter);

    // Clean up database before each test
    mongoTemplate.getDb().drop();
  }

  /**
   * Creates a repository instance
   *
   * @param repositoryClass The repository interface class
   * @return A repository instance
   */
  protected <T> T createRepository(Class<T> repositoryClass) {
    MongoRepositoryFactory factory = new MongoRepositoryFactory(mongoTemplate);
    return factory.getRepository(repositoryClass);
  }


}