/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.BangedSource;
import io.jeffrey.web.sources.MarkdownFilteredSource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.TableOfContentsSource;
import io.jeffrey.web.sources.TagsFilteredSource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Defines a Stage that comes from disk; that is, this will load items from disk
 * and treat them as sources. It also inject markdown and tag filter (which
 * should be factored out)
 *
 * TODO: factor this since stages can come from multiple
 * places
 */
public class DiskLoaderStage extends Stage {
  private static Source loadIfPossible(final File inputFile, final File root) throws IOException {
    final String name = inputFile.getCanonicalPath().substring(root.getCanonicalPath().length() + 1);
    if (!(name.endsWith(".html") || name.endsWith(".markdown"))) {
      return null;
    }
    try {
      Source source = new TagsFilteredSource(new BangedSource(name, new FileReader(inputFile)));
      if (name.endsWith(".markdown")) {
        source = new MarkdownFilteredSource(source, "body");
      }
      // TODO: put a common 'is this content or not'
      final boolean isSnippet = "snippet".equalsIgnoreCase(source.get("type"));
      if (isSnippet) {
        return source;
      }
      final boolean isTemplate = source.get("template-name") != null;
      if (isTemplate) {
        return source;
      }

      return new TableOfContentsSource(source);
    } catch (final Exception err) {
      System.err.println("Skipping:" + inputFile);
      err.printStackTrace();
      return null;
    }
  }

  private static void walk(final ArrayList<Source> _sources, final File path, final File root) throws IOException {
    for (final File inputFile : path.listFiles()) {
      if (inputFile.isDirectory()) {
        walk(_sources, inputFile, root);
      } else if (inputFile.isFile()) {
        final Source source = loadIfPossible(inputFile, root);
        if (source == null) {
          System.err.println("Did not understand the source:" + inputFile.toString());
        } else {
          _sources.add(source);
        }
      }
    }
  }

  private final Collection<Source> sources;

  public DiskLoaderStage(final File inputPathRoot) throws IOException {
    final ArrayList<Source> _sources = new ArrayList<>();
    walk(_sources, inputPathRoot, inputPathRoot);
    this.sources = Collections.unmodifiableCollection(_sources);
  }

  @Override
  public Collection<Source> sources() {
    return sources;
  }
}
