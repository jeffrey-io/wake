package io.jeffrey.web.sources;

import com.github.jknack.handlebars.Handlebars;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class TopologizedSource extends Source {
  private static Handlebars compiler = new Handlebars();
  private final Source source;
  private final Map<String, Object> topology;

  public TopologizedSource(Source source, Map<String, Object> topology) {
    this.source = source;
    this.topology = topology;
  }

  @Override
  public String get(String key) {
    return source.get(key);
  }

  @Override
  public void populateDomain(Set<String> domain) {
    source.populateDomain(domain);
  }

  @Override
  public void walkComplex(BiConsumer<String, Object> injectComplex) {
    source.walkComplex(injectComplex);
    injectComplex.accept("topology", topology);
  }
}
