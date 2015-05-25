package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;
import org.junit.Test;

/**
 * Created by jeffrey on 4/8/14.
 */
public class ApplyTemplateBodySourceTest extends TestingBase {

   @Test
   public void testTemplateApplicationResults() {
      HashMapSource data = createVerySimpleSource();
      data.put("something", "XYZ");
      data.put("other", "AC/DC");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "[{{something}}]=what? {{other}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "[XYZ]=what? AC/DC");
      assertItemization(apply, "body", "something", "other");
   }

   @Test
   public void testTemplatingTailRecursionApplication() {
      HashMapSource data = createVerySimpleSource();
      data.put("place", "ABC");
      data.put("something", "XYZ");
      data.put("something", "{{place}}");
      data.put("other", "AC/DC");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "[{{something}}]=what? {{other}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "[ABC]=what? AC/DC");
      assertItemization(apply, "body", "something", "other", "place");
   }
   
   @Test
   public void testTemplatingWithChildBody() {
      HashMapSource data = createVerySimpleSource();
      data.put("body", "<a href=\"http://FOO\">");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "{{{body}}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "<a href=\"http://FOO\">");
      assertItemization(apply, "body");
   }

   @Test
   public void testTemplatingIsVeryLazy() {
      HashMapSource data = createVerySimpleSource();
      HashMapSource template = createVerySimpleSource();
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      template.put("body", "[{{something}}]=what? {{other}}");
      assertBodyEvaluate(apply, "[]=what? ");
      data.put("place", "ABC");
      data.put("something", "XYZ");
      data.put("something", "{{place}}");
      data.put("other", "AC/DC");
      template.put("body", "[{{something}}]=what? {{other}}");
      assertBodyEvaluate(apply, "[ABC]=what? AC/DC");
      assertItemization(apply, "body", "something", "other", "place");
   }

   @Test
   public void testTemplateDefinesDefault() {
      HashMapSource data = createVerySimpleSource();
      data.put("place", "ABC");
      data.put("something", "XYZ");
      data.put("something", "{{place}}");
      data.put("other", "AC/DC");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "[{{something}}]=what? {{other}}");
      template.put("url", "foo");
      template.put("place", "not here");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "[ABC]=what? AC/DC");
      assertItemization(apply, "body", "something", "other", "place");
      assertEvaluate("url", apply, "foo");
      assertEvaluate("place", apply, "ABC");
   }

   @Test
   public void testDetectInfiniteLoop() {
      HashMapSource data = createVerySimpleSource();
      data.put("place", "data:{{{place}}}");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "place={{{place}}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      try {
         apply.get("body");
         fail("should be exhausted");
      } catch (SourceException se) {

      }
   }

}
