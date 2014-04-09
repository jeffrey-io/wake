package io.jeffrey.web.sources;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
  public void itemize(Consumer<String> itemizer) {
    source.itemize(itemizer);
    itemizer.accept("toc");
  }

  @Override
  public void itemize(BiConsumer<String, Object> inject) {
    source.itemize(inject);
  }
}
