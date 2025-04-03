package com.minionslab.core.api.dto;

import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.enums.MinionType;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MinionResponse {
    private String response;
    private String minionId;
    private String userId;
    private MinionType minionType;
    private Map<String, Object> metadata;
    private String promptName;
    private String promptVersion;
    private Instant effectiveDate;
    private Instant expiryDate;
    private String version;

    public MinionResponse(String response, String minionId) {
        this.response = response;
        this.minionId = minionId;
    }

    public static MinionResponse fromMinion(Minion minion) {
        if (minion == null) {
            throw new IllegalArgumentException("Minion cannot be null");
        }

        // Convert metadata from Map<String, String> to Map<String, Object>
        Map<String, Object> responseMetadata = new HashMap<>();
        if (minion.getMetadata() != null) {
            minion.getMetadata().forEach((k, v) -> responseMetadata.put(k, v));
        }

        return new MinionResponse(null, minion.getMinionId())
            .setMinionType(minion.getMinionType())
            .setMetadata(responseMetadata)
            .setPromptVersion(minion.getMinionPrompt().getVersion())
            .setVersion(minion.getVersion());
    }
} 