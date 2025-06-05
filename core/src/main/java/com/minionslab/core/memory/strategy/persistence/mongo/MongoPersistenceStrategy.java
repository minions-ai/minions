package com.minionslab.core.memory.strategy.persistence.mongo; // Example package


import com.minionslab.core.memory.Memory;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryItem;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository; // Or @Component

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoPersistenceStrategy implements MemoryPersistenceStrategy<Message> {
    
    private static final Logger log = LoggerFactory.getLogger(MongoPersistenceStrategy.class);
    
    private final MongoTemplate mongoTemplate;
    private final MongoQueryTranslator queryConverter;
    
    // Mappers for different MemoryItem types this strategy handles
    private final MessageMongoMapper messageMapper;
    // private final FactMongoMapper factMapper; // Example for another type
    
    // Collection names (could be made configurable)
    private static final String MESSAGES_COLLECTION_NAME = "messages";
    // private static final String FACTS_COLLECTION_NAME = "facts";
    
    @Autowired
    public MongoPersistenceStrategy(MongoTemplate mongoTemplate,
                                    MongoQueryTranslator queryConverter,
                                    MessageMongoMapper messageMapper
            /* FactMongoMapper factMapper */) {
        this.mongoTemplate = mongoTemplate;
        this.queryConverter = queryConverter;
        this.messageMapper = messageMapper;
        // this.factMapper = factMapper;
    }
    
    // --- Helper methods to get collection name and document class ---
    // This section would need to be more robust for many MemoryItem types,
    // potentially using a registry or switch statement.
    private String getCollectionNameForDomainType(Class<? extends MemoryItem> domainItemType) {
        if (Message.class.isAssignableFrom(domainItemType) || SimpleMessage.class.isAssignableFrom(domainItemType)) {
            return MESSAGES_COLLECTION_NAME;
        }
        // if (Fact.class.isAssignableFrom(domainItemType)) {
        //     return FACTS_COLLECTION_NAME;
        // }
        log.warn("No collection name configured for domain type: {}", domainItemType.getName());
        throw new IllegalArgumentException("Unsupported MemoryItem type for collection name: " + domainItemType.getName());
    }
    
    private Class<?> getDocumentClassForDomainType(Class<? extends MemoryItem> domainItemType) {
        if (Message.class.isAssignableFrom(domainItemType) || SimpleMessage.class.isAssignableFrom(domainItemType)) {
            return org.bson.Document.class;
        }
        // if (Fact.class.isAssignableFrom(domainItemType)) {
        //     return FactMongoDocument.class;
        // }
        log.warn("No document class configured for domain type: {}", domainItemType.getName());
        throw new IllegalArgumentException("Unsupported MemoryItem type for document class: " + domainItemType.getName());
    }
    
    @Override
    public <T extends MemoryItem> T save(T item) {
        if (item == null) {
            throw new IllegalArgumentException("MemoryItem to save cannot be null.");
        }
        
        String collectionName = getCollectionNameForDomainType(item.getClass());
        Object mongoDocument; // The persistence-specific document
        
        if (item instanceof Message) {
            mongoDocument = messageMapper.toDocument((Message) item);
        }
        // else if (item instanceof Fact) {
        //     mongoDocument = factMapper.toDocument((Fact) item);
        // }
        else {
            log.error("No mapper available for MemoryItem type: {}", item.getClass().getName());
            throw new UnsupportedOperationException("Save not implemented for type: " + item.getClass().getName());
        }
        
        Object savedMongoDocument = mongoTemplate.save(mongoDocument, collectionName);
        
        // Map back to domain type
        if (savedMongoDocument instanceof org.bson.Document && item instanceof Message) {
            @SuppressWarnings("unchecked")
            T result = (T) messageMapper.toDomain((org.bson.Document) savedMongoDocument);
            return result;
        }
        // else if (savedMongoDocument instanceof FactMongoDocument && item instanceof Fact) {
        //     @SuppressWarnings("unchecked")
        //     T result = (T) factMapper.toDomain((FactMongoDocument) savedMongoDocument);
        //     return result;
        // }
        log.error("Could not map saved document back to domain type: {}", item.getClass().getName());
        throw new IllegalStateException("Failed to map saved document back to domain type for: " + item.getClass().getName());
    }
    
    @Override
    public <T extends MemoryItem> List<T> saveAll(List<T> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        // Naive implementation. For performance, consider MongoTemplate.insertAll()
        // or bulk operations, which would require grouping by type if saving mixed types.
        return items.stream().map(this::save).collect(Collectors.toList());
    }
    
    @Override
    public <T extends MemoryItem> Optional<T> findById(String id, Class<T> itemType) {
        String collectionName = getCollectionNameForDomainType(itemType);
        Class<?> documentClass = getDocumentClassForDomainType(itemType);
        
        Object foundMongoDocument = mongoTemplate.findById(id, documentClass, collectionName);
        
        if (foundMongoDocument == null) {
            return Optional.empty();
        }
        
        if (Message.class.isAssignableFrom(itemType) && foundMongoDocument instanceof org.bson.Document) {
            @SuppressWarnings("unchecked")
            T result = (T) messageMapper.toDomain((org.bson.Document) foundMongoDocument);
            return Optional.of(result);
        }
        // else if (Fact.class.isAssignableFrom(itemType) && foundMongoDocument instanceof FactMongoDocument) {
        //     @SuppressWarnings("unchecked")
        //     T result = (T) factMapper.toDomain((FactMongoDocument) foundMongoDocument);
        //     return Optional.of(result);
        // }
        log.error("No mapper available to convert found document to domain type: {}", itemType.getName());
        return Optional.empty();
    }
    
    @Override
    public <T extends MemoryItem> boolean deleteById(String id, Class<T> itemType) {
        String collectionName = getCollectionNameForDomainType(itemType);
        Class<?> documentClass = getDocumentClassForDomainType(itemType); // Needed for remove if collection stores polymorphic types
        Query query = Query.query(Criteria.where("_id").is(id));
        long deletedCount = mongoTemplate.remove(query, documentClass, collectionName).getDeletedCount();
        return deletedCount > 0;
    }
    
    @Override
    public <T extends MemoryItem> void deleteAllOfType(Class<T> itemType) {
        String collectionName = getCollectionNameForDomainType(itemType);
        Class<?> documentClass = getDocumentClassForDomainType(itemType);
        mongoTemplate.remove(new Query(), documentClass, collectionName); // Or just mongoTemplate.dropCollection(collectionName);
    }
    
    @Override
    public <T extends MemoryItem> long count(Class<T> itemType) {
        String collectionName = getCollectionNameForDomainType(itemType);
        Class<?> documentClass = getDocumentClassForDomainType(itemType);
        return mongoTemplate.count(new Query(), documentClass, collectionName);
    }
    
    @Override
    public List<Message> fetchCandidateMessages(MemoryQuery memoryQuery) {
        Query mongoQuery = queryConverter.translate(memoryQuery);
        // As before, vector search ($vectorSearch stage) would need special handling here if used
        // For now, this assumes queryConverter produces a standard MongoDB find query.
        List<org.bson.Document> documents = mongoTemplate.find(mongoQuery, org.bson.Document.class, MESSAGES_COLLECTION_NAME);
        return documents.stream()
                        .map(messageMapper::toDomain)
                        .collect(Collectors.toList());
    }
}