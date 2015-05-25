package io.jeffrey.web.sources;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Touch the body of a source, and mutate it.
 * @author jeffrey
 */
public class BodyFinalizerSource extends Source {

  @FunctionalInterface
  public interface BodyMutator {
    public String mutate(String body);
  }

  private final Source      source;
  private final BodyMutator mutator;

  public BodyFinalizerSource(final Source source, final BodyMutator mutator) {
    this.source = source;
    this.mutator = mutator;
  }

  @Override
  public String get(final String key) {
    String result = source.get(key);
    if (key.equals("body")) {
      result = mutator.mutate(result);
    }
    return result;
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
