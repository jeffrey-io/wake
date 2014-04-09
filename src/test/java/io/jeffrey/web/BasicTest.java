package io.jeffrey.web;

import io.jeffrey.web.sources.HashMapSource;
import io.jeffrey.web.sources.Source;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by jeffrey on 4/8/14.
 */
public class BasicTest {

   public HashMapSource createVerySimpleSource() {
      HashMap<String, String> map = new HashMap<>();
      map.put("title", "Z' Title");
      map.put("body", "body");
      return new HashMapSource(map);
   }

   public void assertBodyEvaluate(Source source, String expected) {
      String computed = source.get("body");
      if (expected.equals(computed))
         return;
      throw new AssertionError("expected:'" + expected + "', but got '" + computed + "'");
   }

   public void assertItemization(Source source, String... keys){
      final HashSet<String> itemizedKeys = new HashSet<>();
      source.itemize((key) -> {
            itemizedKeys.add(key);
      });
      for(String keyToCheck : keys) {
         if (!itemizedKeys.contains(keyToCheck)) {
            throw new AssertionError("itemization lacked '" + keyToCheck + "'");
         }
      }
   }
}
