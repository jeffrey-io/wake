/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * TODO: make it work
 */
public class TableOfContentsSource extends Source {
   private Source source;

   public TableOfContentsSource(final Source source) {
      this.source = source;
   }

   @Override
   public String get(String key) {
      if ("toc".equalsIgnoreCase("key")) {
         throw new SourceException("I have not yet done this, sorry");
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
