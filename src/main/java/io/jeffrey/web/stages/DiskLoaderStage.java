/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Defines a Stage that comes from disk; that is, this will load itmes from disk and treat them as sources.
 * It also injects markdown and tag filter (which should be factored out)
 * TODO: factor this since stages can come from multiple places
 */
public class DiskLoaderStage extends Stage {
   private final Collection<Source> sources;

   public DiskLoaderStage(File inputPathRoot) {
      ArrayList<Source> _sources = new ArrayList<>();
      for (File inputFile : inputPathRoot.listFiles()) {
         Source source = loadIfPossible(inputFile);
         if (source == null) {
            System.err.println("Did not understand the source:" + inputFile.toString());
         } else {
            _sources.add(source);
         }
      }
      this.sources = Collections.unmodifiableCollection(_sources);
   }

   private Source loadIfPossible(File inputFile) {
      String name = inputFile.getName();
      if (!(name.endsWith(".html") || name.endsWith(".markdown"))) {
         return null;
      }
      try {
         Source source = new TagsFilteredSource(new BangedSource(inputFile.getName(), new FileReader(inputFile)));
         if (name.endsWith(".markdown")) {
            source = new MarkdownFilteredSource(source, "body");
         }
         // TODO: put a common 'is this content or not'
         boolean isSnippet = "snippet".equalsIgnoreCase(source.get("type"));
         if (isSnippet) return source;
         boolean isTemplate = source.get("template-name") != null;
         if (isTemplate) return source;

         return new TableOfContentsSource(source);
      } catch (Exception err) {
         System.err.println("Skipping:" + inputFile);
         err.printStackTrace();
         return null;
      }
   }

   @Override
   public Collection<Source> sources() {
      return sources;
   }
}
