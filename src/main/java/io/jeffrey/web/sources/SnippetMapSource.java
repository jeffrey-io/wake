/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This enables one to take a source and then inject values either above the given source or under the given source (when it is not present).
 */
public class SnippetMapSource extends Source {
  private final Source              real;
  private final Map<String, String> snippetsHigher;
  private final Map<String, String> snippetsLower;

  public SnippetMapSource(final Source real, final Map<String, String> snippetsHigher, final Map<String, String> snippetsLower) {
    this.real = real;
    this.snippetsHigher = snippetsHigher;
    this.snippetsLower = snippetsLower;
  }

  @Override
  public String get(final String key) {
    String value = snippetsHigher.get(key);
    if (value != null) {
      return value;
    }
    value = real.get(key);
    if (value != null) {
      return value;
    }
    return snippetsLower.get(key);
  }

  @Override
  public void populateDomain(final Set<String> domain) {
    domain.addAll(snippetsHigher.keySet());
    real.populateDomain(domain);
    domain.addAll(snippetsLower.keySet());
  }

  @Override
  public void walkComplex(final BiConsumer<String, Object> injectComplex) {
    real.walkComplex(injectComplex);
  }
}
