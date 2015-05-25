/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.markdown4j.Markdown4jProcessor;

/**
 * Apply markdown to the given keys
 */
public class MarkdownFilteredSource extends Source {
  private final Markdown4jProcessor markdown;
  private final Source              source;
  private final HashSet<String>     markdownKeys;

  /**
   * Apply markdown formatting to the given source for the given keys specified in keys
   *
   * @param source
   * @param keys
   */
  public MarkdownFilteredSource(final Source source, final String... keys) {
    this.markdown = new Markdown4jProcessor();
    this.source = source;
    this.markdownKeys = new HashSet<>();
    markdownKeys.add("body");
    for (final String key : keys) {
      markdownKeys.add(key);
    }
  }

  @Override
  public String get(final String key) {
    if (markdownKeys.contains(key)) {
      try {
        final String prior = source.get(key);
        String next = markdown.process(prior);
        // TODO: dig deep into markdown and fix this stupid thing, or rip out markdown4j since it is really old
        next = next.replaceAll(Pattern.quote("http: //"), "http://");
        next = next.replaceAll(Pattern.quote("https: //"), "https://");
        return next;
      } catch (final IOException impossible) {
        throw new RuntimeException(impossible);
      }
    }
    return source.get(key);
  }

  @Override
  public void populateDomain(final Set<String> domain) {
    source.populateDomain(domain);
  }

  @Override
  public void walkComplex(final BiConsumer<String, Object> injectComplex) {
    source.walkComplex(injectComplex);
  }
}
