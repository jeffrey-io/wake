/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.SnippetMapSource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.Source.SourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Filters out the snippets and then injects them into every non-snippet source
 * TODO: document annotations
 */
public class SnippetInjectorStage extends Stage {
  private final Stage prior;

  public SnippetInjectorStage(final Stage prior) {
    this.prior = prior;
  }

  @Override
  public Collection<Source> sources() {
    final ArrayList<Source> preSnippetInjector = new ArrayList<>();
    final HashMap<String, String> snippets = new HashMap<>();
    for (final Source source : prior.sources()) {
      if (source.getType() == SourceType.Snippet) {
        snippets.put(source.get("name"), source.get("body"));
      } else {
        preSnippetInjector.add(source);
      }
    }
    final ArrayList<Source> next = new ArrayList<>();
    for (final Source source : preSnippetInjector) {
      next.add(new SnippetMapSource(source, Collections.emptyMap(), snippets));
    }
    return next;
  }
}
