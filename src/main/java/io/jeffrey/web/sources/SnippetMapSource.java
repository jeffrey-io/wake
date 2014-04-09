package io.jeffrey.web.sources;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by jeffrey on 3/19/2014.
 */
public class SnippetMapSource extends Source {
  private final Source real;
  private final HashMap<String, String> snippets;
  private boolean compiled;

  public SnippetMapSource(Source real, HashMap<String, String> snippets) {
    this.real = real;
    this.snippets = snippets;
  }

  @Override
  public String get(String key) {
    String value = real.get(key);
    if (value != null) return value;
    return snippets.get(key);
  }

  @Override
  public void itemize(Consumer<String> itemizer) {
    real.itemize(itemizer);
    for (String key : snippets.keySet()) itemizer.accept(key);
  }

  @Override
  public void itemize(BiConsumer<String, Object> inject) {
    real.itemize(inject);
  }
}
