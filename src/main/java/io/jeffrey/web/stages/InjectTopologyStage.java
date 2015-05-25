/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.ComplexMapInjectedSource;
import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Defines a tree over the site
 * TODO: document better
 */
public class InjectTopologyStage extends Stage {
  private class TopologyNode {
    private final HashMap<String, TopologyNode> children;
    private final String                        path;
    private Source                              source;

    public TopologyNode(final String path) {
      this.children = new HashMap<>();
      this.path = path;
    }

    public HashMap<String, Object> compile(final Source active) {
      final HashMap<String, Object> result = new HashMap<>();
      result.put("exists", source != null);
      final String activePath = active.get("path");
      if (activePath != null) {
        result.put("child_is_active", activePath.startsWith(path));
      }
      if (source != null) {
        result.put("title", source.get("title"));
        result.put("active", active == source);
        result.put("url", source.get("url"));
      }
      final ArrayList<Object> compiledChildren = new ArrayList<>();
      final ArrayList<TopologyNode> sortedChildren = new ArrayList<>(children.values());
      Collections.sort(sortedChildren, Comparator.comparingLong((item) -> item.order()));
      for (final TopologyNode child : sortedChildren) {
        compiledChildren.add(child.compile(active));
      }
      result.put("children", compiledChildren);
      for (final String childName : children.keySet()) {
        result.put(childName, children.get(childName).compile(active));
      }
      return result;
    }

    private long order() {
      if (source == null) {
        return Integer.MAX_VALUE;
      }
      return source.order();
    }
  }

  private class TopologyTree {
    TopologyNode root;

    private TopologyTree() {
      this.root = new TopologyNode("");
    }

    public void add(final String path, final Source source) {
      final TopologyNode node = node(path, source);
      if (node.source != null) {
        throw new RuntimeException("duplicate path");
      }
      node.source = source;
    }

    public HashMap<String, Object> compile(final Source active) {
      return root.compile(active);
    }

    TopologyNode node(final String path, final Source source) {
      TopologyNode head = root;
      for (String part : path.split(SLASH)) {
        if ("$".equals(part)) {
          part = source.get("name");
        }
        TopologyNode next = head.children.get(part);
        if (next == null) {
          String nextPath = head.path;
          if (nextPath.length() > 0) {
            nextPath += "/";
          }
          nextPath += part;
          next = new TopologyNode(nextPath);
          head.children.put(part, next);
        }
        head = next;
      }
      return head;
    }
  }

  private static final String SLASH = Pattern.quote("/");

  private final Stage         priorStage;

  public InjectTopologyStage(final Stage priorStage) {
    this.priorStage = priorStage;
  }

  @Override
  public Collection<Source> sources() {
    final ArrayList<Source> attachTo = new ArrayList<>();
    final TopologyTree topology = new TopologyTree();
    for (final Source source : priorStage.sources()) {
      final String path = source.get("path");
      if (path != null) {
        topology.add(path, source);
      }
      attachTo.add(source);
    }
    final ArrayList<Source> next = new ArrayList<>();
    attachTo.forEach((random) -> {
      next.add(new ComplexMapInjectedSource(random, "topology", topology.compile(random)));
    });
    return next;
  }
}
