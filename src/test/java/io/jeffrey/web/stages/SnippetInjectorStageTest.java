package io.jeffrey.web.stages;

import io.jeffrey.web.TestingBase;
import io.jeffrey.web.sources.HashMapSource;
import io.jeffrey.web.sources.Source;

/**
 * Created by jeffrey on 4/9/14.
 */
public class SnippetInjectorStageTest extends TestingBase {

   public void testSnippetMerged() {
      HashMapSource snippet = createVerySimpleSource();
      snippet.put("type", "snippet");
      snippet.put("name", "meme");
      snippet.put("body", "cowboy");

      HashMapSource raw = createVerySimpleSource();
      raw.put("me", "me");

      Stage stages = stageOf(snippet, raw);
      SnippetInjectorStage snippets = new SnippetInjectorStage(stages);

      Source merged = getExactlyOne(snippets);
      assertEvaluate("me", merged, "me");
      assertEvaluate("meme", merged, "cowboy");
   }
}
