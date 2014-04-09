package io.jeffrey.web.sources;

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class MarkdownFilteredSource extends Source {
  private final Source source;
  private final HashSet<String> markdownKeys;
  private Markdown4jProcessor markdown;

  public MarkdownFilteredSource(final Source source, String... extraKeys) {
    this.markdown = new Markdown4jProcessor();
    this.source = source;
    this.markdownKeys = new HashSet<>();
    markdownKeys.add("body");
    for (String key : extraKeys) markdownKeys.add(key);
  }

  @Override
  public String get(String key) {
    if (markdownKeys.contains(key)) {
      try {
        return markdown.process(source.get(key));
      } catch (IOException impossible) {
        throw new RuntimeException(impossible);
      }
    }
    return source.get(key);
  }

  @Override
  public void populateDomain(Set<String> domain) {
     source.populateDomain(domain);
  }
  @Override
  public void walkComplex(BiConsumer<String, Object> injectComplex) {
    source.walkComplex(injectComplex);
  }
}
