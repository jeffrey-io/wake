package io.jeffrey.web.stages;

import io.jeffrey.web.TestingBase;
import io.jeffrey.web.sources.HashMapSource;
import io.jeffrey.web.sources.Source;

import org.junit.Test;

public class BodyFinalizerStageTest extends TestingBase {
  
  @Test
  public void testMutator() {
    HashMapSource snippet = createVerySimpleSource();
    snippet.put("body", "cowboy");
    snippet.put("x", "xyz");
    BodyFinalizerStage fstage = new BodyFinalizerStage(stageOf(snippet), (body) -> {
      return "|" + body + "|";
    });
    Source done = getExactlyOne(fstage);
    assertEvaluate("body", done, "|cowboy|");
    assertEvaluate("x", done, "xyz");
  }
}
