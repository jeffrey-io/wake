package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;
import org.junit.Test;

/**
 * Created by jeffrey on 4/9/14.
 */
public class TagsFilteredSourceTest extends TestingBase {

   @Test
   public void testTagDetection() {
      HashMapSource raw = createVerySimpleSource();
      raw.put("body", "&&a&&");
      TagsFilteredSource tagged = new TagsFilteredSource(raw);
      assertBodyEvaluate(tagged, "<em class=\"tag\">a</em>");
   }

   @Test
   public void testTagDetectionAdj() {
      HashMapSource raw = createVerySimpleSource();
      raw.put("body", "&&a&&&&b&&");
      TagsFilteredSource tagged = new TagsFilteredSource(raw);
      assertBodyEvaluate(tagged, "<em class=\"tag\">a</em><em class=\"tag\">b</em>");
   }

   @Test
   public void testTagDetectionAdjAndMixed() {
      HashMapSource raw = createVerySimpleSource();
      raw.put("body", "X&&a&&Y&&b&&Z");
      TagsFilteredSource tagged = new TagsFilteredSource(raw);
      assertBodyEvaluate(tagged, "X<em class=\"tag\">a</em>Y<em class=\"tag\">b</em>Z");
   }
}
