package io.jeffrey.web;

import io.jeffrey.web.sources.HashMapSource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.stages.SetStage;
import io.jeffrey.web.stages.Stage;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by jeffrey on 4/8/14.
 */
public class TestingBase {

   private static String lastTest = "?";

   public HashMapSource createVerySimpleSource() {
      HashMap<String, String> map = new HashMap<>();
      map.put("title", "Z' Title");
      map.put("body", "body");
      return new HashMapSource(map);
   }

   protected void logCheck(String msg) {
      String test = "?";
      try {
         throw new NullPointerException();
      } catch (NullPointerException npe) {
         for (StackTraceElement ste : npe.getStackTrace()) {
            if (ste.getMethodName().startsWith("test")) {
               test = ste.getMethodName().substring(4);
            }
         }
      }
      if (!lastTest.equals(test)) {
         System.out.println();
         System.out.println("begin[" + test + "]");
         lastTest = test;
      }
      System.out.println("check[" + test + "]:" + msg);
   }

   protected void assertEvaluate(String key, Source source, String expected) {
      String computed = source.get(key);
      logCheck(key + ":'" + computed + "'='" + expected + "'");
      if (expected.equals(computed))
         return;
      throw new AssertionError("expected:'" + expected + "', but got '" + computed + "'");
   }

   protected void assertBodyEvaluate(Source source, String expected) {
      assertEvaluate("body", source, expected);
   }

   protected void fail(String why) {
      throw new AssertionError("we expected to fail:" + why);
   }

   protected void assertItemization(Source source, String... keys) {
      final HashSet<String> itemizedKeys = new HashSet<>();
      source.populateDomain(itemizedKeys);
      for (String keyToCheck : keys) {
         logCheck(itemizedKeys.toString() + " contains '" + keyToCheck + "'");
         if (!itemizedKeys.contains(keyToCheck)) {
            throw new AssertionError("itemization lacked '" + keyToCheck + "'");
         }
      }
   }

   protected void assertEquals(String expected, String computed) {
      logCheck("'" + computed + "'='" + expected + "'");
      if (expected.equals(computed))
         return;
      throw new AssertionError("expected:'" + expected + "', but got '" + computed + "'");
   }

   protected Reader readerize(String value) {
      return new InputStreamReader(new ByteArrayInputStream(value.getBytes()));
   }

   protected Stage stageOf(Source... sources) {
      HashSet<Source> set = new HashSet<>();
      for (Source source : sources) set.add(source);
      return new SetStage(set);
   }

   protected Source getExactlyOne(Stage stages) {
      Collection<Source> sources = stages.sources();
      logCheck("one size check:" + sources.size() + ":" + stages.getClass().getName());
      if (1 != sources.size()) {
         throw new AssertionError("size was not 1");
      }
      return sources.iterator().next();
   }

}
