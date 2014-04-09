package io.jeffrey.web.stages;

import io.jeffrey.web.sources.SnippetMapSource;
import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by jeffrey on 3/19/2014.
 */
public class SnippetInjector extends Stage {
  private final Stage prior;

  public SnippetInjector(final Stage prior) {
    this.prior = prior;
  }

  @Override
  public Collection<Source> sources() {
    ArrayList<Source> preSnippetInjector = new ArrayList<>();
    HashMap<String, String> snippets = new HashMap<>();
    for (Source source : prior.sources()) {
      boolean isSnippet = "snippet".equalsIgnoreCase(source.get("type"));
      if (!isSnippet) {
        preSnippetInjector.add(source);
      } else {
        snippets.put(source.get("name"), source.get("body"));
      }
    }
    ArrayList<Source> next = new ArrayList<>();
    for (Source source : preSnippetInjector) {
      next.add(new SnippetMapSource(source, snippets));
    }
    return next;
  }
}
