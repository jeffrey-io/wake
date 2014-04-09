package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;

import java.util.HashMap;

/**
 * Created by jeffrey on 4/9/14.
 */
public class SnippetMapSourceTest extends TestingBase {

   public void testInjectionAndPrecedender() {
      HashMapSource real = createVerySimpleSource();
      real.put("keep", "me");
      real.put("hide", "real");
      HashMap<String, String> over = new HashMap<>();
      HashMap<String, String> under = new HashMap<>();
      over.put("hide", "over");
      under.put("hide", "under");
      over.put("over", "1");
      under.put("under", "2");
      SnippetMapSource withSnippets = new SnippetMapSource(real, over, under);
      assertEvaluate("keep", withSnippets, "me");
      assertEvaluate("hide", withSnippets, "over");
      assertEvaluate("over", withSnippets, "1");
      assertEvaluate("under", withSnippets, "2");

      assertItemization(withSnippets, "keep", "hide", "over", "under");
   }
}
