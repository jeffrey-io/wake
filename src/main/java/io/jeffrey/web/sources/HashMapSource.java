/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Primarily for testing, but this enables one to just "define" a source easily
 */
public class HashMapSource extends Source {

  private final HashMap<String, String> data;

  public HashMapSource(final Map<String, String> map) {
    this.data = new HashMap<>(map);
  }

  @Override
  public String get(final String key) {
    return data.get(key);
  }

  @Override
  public void populateDomain(final Set<String> domain) {
    domain.addAll(data.keySet());
  }

  public HashMapSource put(final String key, final String value) {
    this.data.put(key, value);
    return this;
  }

  @Override
  public void walkComplex(final BiConsumer<String, Object> injectComplex) {
  }
}
