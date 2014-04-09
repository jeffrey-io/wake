package io.jeffrey.web.sources;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A source is basically a very special key value pair map where we lazily enable redefinition of the map.
 * <p>
 * The idea is that you can ask a source for any key, and it will return the value if it exists (null otherwise).
 * <p>
 * The kicker (and the real novel idea here) is that the the source has no standard domain/keySet.
 * Instead, you ask for the domain and it will tell you what keys it provides. The nice thing about this is a wrapping layer may intercept that key and use it.
 */
public abstract class Source implements Comparable<Source> {
   /**
    * get the value from the source
    *
    * @param key the key (found in the set that is populated via populateDomain; "body" is a fairly common key)
    * @return
    */
   public abstract String get(String key);

   /**
    * Get the given key from the source, but it must be present or else we abort the entire thing
    *
    * @param key
    * @return
    */
   public String getRequired(String key) {
      String value = get(key);
      if (value == null) {
         throw new SourceException("required field '" + key + "' not present");
      }
      return value;
   }

   /**
    * Populate the given set with all the possible strings that can be acquired from the get() method
    *
    * @param domain
    */
   public abstract void populateDomain(Set<String> domain);

   /**
    * A source may define things too complicated to return as a string, so instead, a source may provide things
    * in the form of an Object that the top level can decide what to do with
    *
    * @param injectComplex
    */
   public abstract void walkComplex(BiConsumer<String, Object> injectComplex);

   /**
    * Get the order from the object; if you sort this
    *
    * @return
    */
   public long order() {
      String ord = get("order");
      if (ord == null) return Integer.MAX_VALUE;
      return Integer.parseInt(ord);
   }

   @Override
   public int compareTo(Source o) {
      return Long.compare(this.order(), o.order());
   }
}
