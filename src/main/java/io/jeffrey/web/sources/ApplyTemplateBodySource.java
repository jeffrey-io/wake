/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import com.github.jknack.handlebars.Handlebars;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This source merges two sources together by taking a 'data' source along with a 'template' source. The results are merged together such
 */
public final class ApplyTemplateBodySource extends Source {
   private static final int MAX_SUBSTITUTIONS = 128;
   private static final Handlebars compiler = new Handlebars();
   private final Source data;
   private final Source template;

   /**
    * construct
    *
    * @param data     where the data comes from
    * @param template what defines the template; the 'body' will be constructed from the template's body evaluated with the data substituted in via handlebars like {{key}}
    */
   public ApplyTemplateBodySource(Source data, Source template) {
      this.data = data;
      this.template = template;
   }

   @Override
   public String get(String key) {
      if ("body".equals(key)) {
         try {
            // the substitution map
            HashMap<String, Object> map = new HashMap<>();
            HashSet<String> domain = new HashSet<>();
            populateDomain(domain);
            for (String item : domain) {
               map.put(item, data.get(item));
            }
            walkComplex((k, v) -> {
               map.put(k, v);
            });
            // render the body template
            String ret = template.get("body");

            // until the hash code stablizie
            long hash = ret.hashCode();
            long last = -1;
            int round = 0;
            while (hash != last) {
               if (round > MAX_SUBSTITUTIONS) {
                  // TODO: do a simple regex to advise as to the problematic key
                  throw new SourceException("sorry, but we have exhausted our limit of substitutions of " + MAX_SUBSTITUTIONS + ";");
               }
               round++;
               last = hash;
               // do it
               ret = compiler.compileInline(ret).apply(map);
               hash = ret.hashCode();
            }
            return ret;
         } catch (IOException failed) {
            // should be impossible as this is all in-memory stuff,
            throw new SourceException(failed.getMessage() + " impossible!");
         }
      }
      String returnValueIfNotNull = data.get(key);
      if (returnValueIfNotNull != null)
         return returnValueIfNotNull;
      return template.get(key);
   }

   @Override
   public void populateDomain(Set<String> domain) {
      template.populateDomain(domain);
      data.populateDomain(domain);
   }

   @Override
   public void walkComplex(BiConsumer<String, Object> injectComplex) {
      template.walkComplex(injectComplex);
      data.walkComplex(injectComplex);
   }
}
