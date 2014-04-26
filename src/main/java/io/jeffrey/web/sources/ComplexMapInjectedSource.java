/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This enables one to inject complex maps (i.e. topologies and other objects) into the source for during the 'walkComplex' phase of rendering
 */
public class ComplexMapInjectedSource extends Source {
   private final Source source;
   private final String mapKey;
   private final Object mapValue;

   public ComplexMapInjectedSource(Source source, String mapKey, Object mapValue) {
      this.source = source;
      this.mapKey = mapKey;
      this.mapValue = mapValue;
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
      injectComplex.accept(mapKey, mapValue);
   }
}
