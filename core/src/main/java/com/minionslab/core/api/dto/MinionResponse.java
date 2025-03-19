package com.minionslab.core.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MinionResponse {
    private String response;
    private String minionId;

    public MinionResponse(String response, String minionId) {
        this.response = response;
        this.minionId = minionId;

    }

    // Getters and Setters
} 