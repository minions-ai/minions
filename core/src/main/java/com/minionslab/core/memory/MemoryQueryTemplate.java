package com.minionslab.core.memory;

import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MemoryQueryTemplate {
    private final Set<MemoryQuery.MemorySubsystem> subsystems;
    private final MessageRole role;
    private final MessageScope scope;
    private final String keyword;
    private final Instant after;
    private final Instant before;
    private final Map<String, Object> metadata;
    private final String entityType;
    private final Integer limit;

    private MemoryQueryTemplate(Builder builder) {
        this.subsystems = builder.subsystems;
        this.role = builder.role;
        this.scope = builder.scope;
        this.keyword = builder.keyword;
        this.after = builder.after;
        this.before = builder.before;
        this.metadata = builder.metadata != null ? Collections.unmodifiableMap(new HashMap<>(builder.metadata)) : null;
        this.entityType = builder.entityType;
        this.limit = builder.limit;
    }

    public static Builder builder() { return new Builder(); }

    public MemoryQuery toMemoryQuery() {
        return MemoryQuery.builder()
                .subsystems(subsystems)
                .role(role)
                .scope(scope)
                .keyword(keyword)
                .after(after)
                .before(before)
                .metadata(metadata)
                .entityType(entityType)
                .limit(limit != null ? limit : 0)
                .build();
    }

    public MemoryQueryTemplate merge(MemoryQueryTemplate other) {
        if (other == null) return this;
        Builder merged = new Builder();
        merged.subsystems = union(this.subsystems, other.subsystems);
        merged.role = this.role != null ? this.role : other.role;
        merged.scope = this.scope != null ? this.scope : other.scope;
        merged.keyword = this.keyword != null ? this.keyword : other.keyword;
        merged.after = this.after != null ? this.after : other.after;
        merged.before = this.before != null ? this.before : other.before;
        merged.metadata = unionMap(this.metadata, other.metadata);
        merged.entityType = this.entityType != null ? this.entityType : other.entityType;
        merged.limit = this.limit != null ? this.limit : other.limit;
        return merged.build();
    }

    public MemoryQueryTemplate union(MemoryQueryTemplate other) {
        if (other == null) return this;
        Builder merged = new Builder();
        merged.subsystems = union(this.subsystems, other.subsystems);
        merged.role = this.role != null ? this.role : other.role;
        merged.scope = this.scope != null ? this.scope : other.scope;
        merged.keyword = this.keyword != null ? this.keyword : other.keyword;
        merged.after = this.after != null ? this.after : other.after;
        merged.before = this.before != null ? this.before : other.before;
        merged.metadata = unionMap(this.metadata, other.metadata);
        merged.entityType = this.entityType != null ? this.entityType : other.entityType;
        merged.limit = this.limit != null ? this.limit : other.limit;
        return merged.build();
    }

    public MemoryQueryTemplate subtract(MemoryQueryTemplate other) {
        if (other == null) return this;
        Builder result = new Builder();
        result.subsystems = subtract(this.subsystems, other.subsystems);
        result.role = (this.role != null && !this.role.equals(other.role)) ? this.role : null;
        result.scope = (this.scope != null && !this.scope.equals(other.scope)) ? this.scope : null;
        result.keyword = (this.keyword != null && !this.keyword.equals(other.keyword)) ? this.keyword : null;
        result.after = (this.after != null && !this.after.equals(other.after)) ? this.after : null;
        result.before = (this.before != null && !this.before.equals(other.before)) ? this.before : null;
        result.metadata = subtractMap(this.metadata, other.metadata);
        result.entityType = (this.entityType != null && !this.entityType.equals(other.entityType)) ? this.entityType : null;
        result.limit = (this.limit != null && !this.limit.equals(other.limit)) ? this.limit : null;
        return result.build();
    }

    public MemoryQueryTemplate intersection(MemoryQueryTemplate other) {
        if (other == null) return null;
        Builder result = new Builder();
        result.subsystems = intersection(this.subsystems, other.subsystems);
        result.role = (this.role != null && this.role.equals(other.role)) ? this.role : null;
        result.scope = (this.scope != null && this.scope.equals(other.scope)) ? this.scope : null;
        result.keyword = (this.keyword != null && this.keyword.equals(other.keyword)) ? this.keyword : null;
        result.after = (this.after != null && this.after.equals(other.after)) ? this.after : null;
        result.before = (this.before != null && this.before.equals(other.before)) ? this.before : null;
        result.metadata = intersectionMap(this.metadata, other.metadata);
        result.entityType = (this.entityType != null && this.entityType.equals(other.entityType)) ? this.entityType : null;
        result.limit = (this.limit != null && this.limit.equals(other.limit)) ? this.limit : null;
        return result.build();
    }

    private static <T> Set<T> union(Set<T> a, Set<T> b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        java.util.Set<T> result = new java.util.HashSet<>(a);
        result.addAll(b);
        return result;
    }

    private static <T> Set<T> subtract(Set<T> a, Set<T> b) {
        if (a == null) return null;
        if (b == null) return a;
        java.util.Set<T> result = new java.util.HashSet<>(a);
        result.removeAll(b);
        return result;
    }

    private static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        if (a == null || b == null) return null;
        java.util.Set<T> result = new java.util.HashSet<>(a);
        result.retainAll(b);
        return result;
    }

    private static <K, V> Map<K, V> unionMap(Map<K, V> a, Map<K, V> b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        Map<K, V> result = new HashMap<>(a);
        result.putAll(b);
        return result;
    }

    private static <K, V> Map<K, V> subtractMap(Map<K, V> a, Map<K, V> b) {
        if (a == null) return null;
        if (b == null) return a;
        Map<K, V> result = new HashMap<>(a);
        for (K key : b.keySet()) {
            result.remove(key);
        }
        return result;
    }

    private static <K, V> Map<K, V> intersectionMap(Map<K, V> a, Map<K, V> b) {
        if (a == null || b == null) return null;
        Map<K, V> result = new HashMap<>();
        for (K key : a.keySet()) {
            if (b.containsKey(key) && a.get(key).equals(b.get(key))) {
                result.put(key, a.get(key));
            }
        }
        return result;
    }

    public static class Builder {
        private Set<MemoryQuery.MemorySubsystem> subsystems;
        private MessageRole role;
        private MessageScope scope;
        private String keyword;
        private Instant after;
        private Instant before;
        private Map<String, Object> metadata;
        private String entityType;
        private Integer limit;

        public Builder subsystems(Set<MemoryQuery.MemorySubsystem> subsystems) { this.subsystems = subsystems; return this; }
        public Builder role(MessageRole role) { this.role = role; return this; }
        public Builder scope(MessageScope scope) { this.scope = scope; return this; }
        public Builder keyword(String keyword) { this.keyword = keyword; return this; }
        public Builder after(Instant after) { this.after = after; return this; }
        public Builder before(Instant before) { this.before = before; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder limit(Integer limit) { this.limit = limit; return this; }
        public MemoryQueryTemplate build() { return new MemoryQueryTemplate(this); }
    }

    // Getters for all fields (optional, add as needed)
    public Optional<Set<MemoryQuery.MemorySubsystem>> getSubsystems() { return Optional.ofNullable(subsystems); }
    public Optional<MessageRole> getRole() { return Optional.ofNullable(role); }
    public Optional<MessageScope> getScope() { return Optional.ofNullable(scope); }
    public Optional<String> getKeyword() { return Optional.ofNullable(keyword); }
    public Optional<Instant> getAfter() { return Optional.ofNullable(after); }
    public Optional<Instant> getBefore() { return Optional.ofNullable(before); }
    public Optional<Map<String, Object>> getMetadata() { return Optional.ofNullable(metadata); }
    public Optional<String> getEntityType() { return Optional.ofNullable(entityType); }
    public Optional<Integer> getLimit() { return Optional.ofNullable(limit); }
}
