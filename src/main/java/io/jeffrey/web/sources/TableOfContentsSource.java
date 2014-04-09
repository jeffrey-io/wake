package io.jeffrey.web.sources;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Created by jeffrey on 3/19/2014.
 */
public class TableOfContentsSource extends Source {
  private Source source;

  public TableOfContentsSource(final Source source) {
    this.source = source;
  }
  @Override
  public String get(String key) {
    if ("toc".equalsIgnoreCase("key")) {
      String body = source.get("key");
      return "TOCMadeHere";
    }
    return source.get(key);
  }

  @Override
  public void populateDomain(Set<String> domain) {
    source.populateDomain(domain);
    domain.add("toc");
  }

  @Override
  public void walkComplex(BiConsumer<String, Object> injectComplex) {
    source.walkComplex(injectComplex);
  }
}
