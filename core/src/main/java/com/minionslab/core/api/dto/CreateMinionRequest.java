package com.minionslab.core.api.dto;

import com.minionslab.core.domain.enums.MinionType;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMinionRequest {
    @NotNull
    private String userId;
    
    @NotNull
    private MinionType minionType;
    

    
    // Add any other metadata fields needed for minion creation
    private Map<String, String> metadata;
    private String promptName;
    private String version;


}