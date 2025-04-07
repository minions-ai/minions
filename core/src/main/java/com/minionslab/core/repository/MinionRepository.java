package com.minionslab.core.repository;

import com.minionslab.core.domain.Minion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Minion entities.
 * Provides basic CRUD operations and custom query methods.
 */
@Repository
public interface MinionRepository extends MongoRepository<Minion, String> {

    /**
     * Find all minions by tenant ID.
     *
     * @param tenantId The tenant ID to search for
     * @return List of minions belonging to the tenant
     */
    List<Minion> findAllByTenantId(String tenantId);

    /**
     * Find a minion by its ID and tenant ID.
     *
     * @param id The minion ID
     * @param tenantId The tenant ID
     * @return Optional containing the minion if found
     */
    Optional<Minion> findByIdAndTenantId(String id, String tenantId);

    /**
     * Check if a minion exists by its ID and tenant ID.
     *
     * @param id The minion ID
     * @param tenantId The tenant ID
     * @return true if the minion exists, false otherwise
     */
    boolean existsByIdAndTenantId(String id, String tenantId);
} 