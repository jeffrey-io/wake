package io.jeffrey.web.sources;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by jeffrey on 3/18/2014.
 */
public abstract class Source {
  /**
   * get the value from the source
   *
   * @param key
   * @return
   */
  public abstract String get(String key);

  public String getRequired(String key) {
    String value = get(key);
    if (value == null) {
      throw new SourceException("required field '" + key + "' not present");
    }
    return value;
  }

  public abstract void itemize(Consumer<String> itemizer);

  public abstract void itemize(BiConsumer<String, Object> inject);

  public long order() {
    String ord = get("order");
    if (ord == null) return Integer.MAX_VALUE;
    return Integer.parseInt(ord);
  }
}
