package io.jeffrey.web.sources;

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Apply markdown to the given keys
 */
public class MarkdownFilteredSource extends Source {
   private final Markdown4jProcessor markdown;
   private final Source source;
   private final HashSet<String> markdownKeys;

   /**
    * Apply markdown formatting to the given source for the given keys specified in keys
    * @param source
    * @param keys
    */
   public MarkdownFilteredSource(final Source source, String... keys) {
      this.markdown = new Markdown4jProcessor();
      this.source = source;
      this.markdownKeys = new HashSet<>();
      markdownKeys.add("body");
      for (String key : keys) {
         markdownKeys.add(key);
      }
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
