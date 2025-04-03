package com.minionslab.core.api.dto;

import com.minionslab.core.domain.enums.MinionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

/**
 * DTO for creating a new Minion.
 * This class represents the request body for creating a new Minion entity.
 * It includes validation constraints and documentation for each field.
 */
@Data
@Accessors(chain = true)
@Validated
@SuperBuilder
@NoArgsConstructor
public class CreateMinionRequest {

    /**
     * The type of the Minion to be created.
     * This field is required and must be a valid MinionType enum value.
     */
    @NotNull(message = "Minion type is required")
    private MinionType minionType;

    /**
     * Additional metadata for the Minion.
     * Optional field that can contain any additional properties.
     * Maximum size is 1000 characters when serialized to JSON.
     */
    @Size(max = 1000, message = "Metadata size exceeds maximum limit of 1000 characters")
    private Map<String, Object> metadata;

    /**
     * The ID of the Prompt entity associated with this Minion.
     * This field is required and must not be blank.
     */
    @NotBlank(message = "Prompt entity ID is required")
    @Size(max = 100, message = "Prompt entity ID must not exceed 100 characters")
    private String promptEntityId;

    /**
     * Version identifier for the Minion.
     * Optional field that can be used for versioning.
     */
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    /**
     * The date when this Minion should become effective.
     * Optional field. If not provided, the Minion becomes effective immediately.
     */
    private Instant effectiveDate;

    /**
     * The date when this Minion should expire.
     * Optional field. If not provided, the Minion does not expire.
     */
    private Instant expiryDate;

    /**
     * Validates the business rules for the request.
     * This method checks:
     * 1. If expiryDate is provided, it must be after effectiveDate
     * 2. If effectiveDate is provided, it must not be in the past
     * 
     * @throws IllegalArgumentException if any business rules are violated
     */
    public void validateBusinessRules() {
        if (effectiveDate != null && effectiveDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Effective date cannot be in the past");
        }

        if (expiryDate != null && effectiveDate != null && expiryDate.isBefore(effectiveDate)) {
            throw new IllegalArgumentException("Expiry date must be after effective date");
        }
    }

    /**
     * Creates a builder with default values.
     * Sets the current timestamp as the effective date.
     * 
     * @return CreateMinionRequestBuilder with default values
     */
    public static CreateMinionRequestBuilder<?, ?> builder() {
        return new CreateMinionRequestBuilderImpl()
            .effectiveDate(Instant.now());
    }
}