package com.minionslab.core.memory;

import java.util.Map;

public class MemorySnapshot {
    private final Map<String, Object> snapshotData;

    public MemorySnapshot(Map<String, Object> snapshotData) {
        this.snapshotData = snapshotData;
    }

    public Map<String, Object> getSnapshotData() {
        return snapshotData;
    }
} 