package com.minionsai.claude.context;

/**
 * Context resolver types for different resolution strategies
 */
public enum ResolverType {
  NEAREST_MATCH,     // Search up the tree for the nearest match
  COMPOSITE,         // Combine values from all contexts in the hierarchy
  ROOT_ONLY,         // Only look at the root context
  LEAF_ONLY,         // Only look at the leaf context
  MERGE_UP           // Merge values from leaf to root
}
