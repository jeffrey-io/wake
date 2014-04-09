package io.jeffrey.web.sources;

import io.jeffrey.web.BasicTest;

/**
 * Created by jeffrey on 4/8/14.
 */
public class ApplyTemplateBodySourceTest extends BasicTest {

   public void testTemplateApplicationResults() {
      HashMapSource data = createVerySimpleSource();
      data.put("something","XYZ");
      data.put("other","AC/DC");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "[{{something}}]=what? {{other}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "[XYZ]=what? AC/DC");
      assertItemization(apply, "body", "something", "other");
   }

   public void testTemplatingTailRecursionApplication() {
      HashMapSource data = createVerySimpleSource();
      data.put("place","ABC");
      data.put("something","XYZ");
      data.put("something","{{place}}");
      data.put("other","AC/DC");
      HashMapSource template = createVerySimpleSource();
      template.put("body", "[{{something}}]=what? {{other}}");
      ApplyTemplateBodySource apply = new ApplyTemplateBodySource(data, template);
      assertBodyEvaluate(apply, "[ABC]=what? AC/DC");
      assertItemization(apply, "body", "something", "other", "place");
   }

}
