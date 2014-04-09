package io.jeffrey.web.sources;

import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

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
   public void populateDomain(Set<String> domain) {
      real.populateDomain(domain);
      domain.addAll(snippets.keySet());
   }

   @Override
   public void walkComplex(BiConsumer<String, Object> injectComplex) {
      real.walkComplex(injectComplex);
   }
}
