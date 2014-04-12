/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.ComplexMapInjectedSource;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Defines a tree over the content
 * TODO: document better
 */
public class InjectTopologyStage extends Stage {
   private static final String SLASH = Pattern.quote("/");
   private final Stage priorStage;

   public InjectTopologyStage(Stage priorStage) {
      this.priorStage = priorStage;
   }

   @Override
   public Collection<Source> sources() {
      ArrayList<Source> attachTo = new ArrayList<>();
      TopologyTree topology = new TopologyTree();
      for (Source source : priorStage.sources()) {
         String path = source.get("path");
         if (path != null) {
            topology.add(path, source);
         }
         attachTo.add(source);
      }
      ArrayList<Source> next = new ArrayList<>();
      for (Source random : attachTo) {
         next.add(new ComplexMapInjectedSource(random, "topology", topology.compile(random)));
      }
      return next;
   }

   private class TopologyNode {
      private final HashMap<String, TopologyNode> children;
      private final String path;
      private Source source;

      public TopologyNode(String path) {
         this.children = new HashMap<>();
         this.path = path;
      }

      private long order() {
         if (source == null) return Integer.MAX_VALUE;
         return source.order();
      }

      public HashMap<String, Object> compile(Source active) {
         HashMap<String, Object> result = new HashMap<>();
         result.put("exists", source != null);
         String activePath = active.get("path");
         if (activePath != null) {
            result.put("child_is_active", activePath.startsWith(path));
         }
         if (source != null) {
            result.put("title", source.get("title"));
            result.put("active", active == source);
            result.put("url", source.get("url"));
         }
         ArrayList<Object> compiledChildren = new ArrayList<>();
         ArrayList<TopologyNode> sortedChildren = new ArrayList<>(children.values());
         Collections.sort(sortedChildren, Comparator.comparingLong((item) -> item.order()));
         for (TopologyNode child : sortedChildren) {
            compiledChildren.add(child.compile(active));
         }
         result.put("children", compiledChildren);
         for (String childName : children.keySet()) {
            result.put(childName, children.get(childName).compile(active));
         }
         return result;
      }
   }

   private class TopologyTree {
      TopologyNode root;

      private TopologyTree() {
         this.root = new TopologyNode("");
      }

      TopologyNode node(String path, Source source) {
         TopologyNode head = root;
         for (String part : path.split(SLASH)) {
            if ("$".equals(part)) {
               part = source.get("name");
            }
            TopologyNode next = head.children.get(part);
            if (next == null) {
               String nextPath = head.path;
               if (nextPath.length() > 0) nextPath += "/";
               nextPath += part;
               next = new TopologyNode(nextPath);
               head.children.put(part, next);
            }
            head = next;
         }
         return head;
      }

      public void add(String path, Source source) {
         TopologyNode node = node(path, source);
         if (node.source != null) throw new RuntimeException("duplicate path");
         node.source = source;
      }

      public HashMap<String, Object> compile(Source active) {
         return root.compile(active);
      }
   }
}
