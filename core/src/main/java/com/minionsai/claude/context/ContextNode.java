package com.minionsai.claude.context;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A node in the context hierarchy tree
 */
@Data
@Builder
@RequiredArgsConstructor
public class ContextNode {

  private String id;
  private ContextNode parent;
  private MinionContext context;
  private ContextNodeType type;

  @Builder.Default
  private List<ContextNode> children = new ArrayList<>();

  public ContextNode(String root, ContextNode o, MinionContext minionContext, ContextNodeType contextNodeType) {
    id = root;
    parent = null;
    context = minionContext;
    type = contextNodeType;
  }

  /**
   * Adds a child context node
   */
  public void addChild(ContextNode child) {
    if (!children.contains(child)) {
      children.add(child);
    }
  }

  /**
   * Removes a child context node
   */
  public void removeChild(ContextNode child) {
    children.remove(child);
  }

  /**
   * Checks if this is a leaf node (no children)
   */
  public boolean isLeaf() {
    return children.isEmpty();
  }

  /**
   * Checks if this is the root node (no parent)
   */
  public boolean isRoot() {
    return parent == null;
  }

  /**
   * Gets the depth of this node in the hierarchy
   */
  public int getDepth() {
    int depth = 0;
    ContextNode current = this;
    while (current.getParent() != null) {
      depth++;
      current = current.getParent();
    }
    return depth;
  }
}
