package io.jeffrey.web.stages;

import io.jeffrey.web.TestingBase;
import io.jeffrey.web.sources.HashMapSource;
import io.jeffrey.web.sources.Source;

import java.util.Collection;

/**
 * Created by jeffrey on 4/9/14.
 */
public class SortByOrderStageTest extends TestingBase {

   private Source orderedSource(int order) {
      HashMapSource source = createVerySimpleSource();
      source.put("order", "" + order);
      return source;
   }

   public void testOrdering() {
      Stage stage = stageOf(orderedSource(6), orderedSource(1), orderedSource(4), orderedSource(2), orderedSource(0), orderedSource(3), orderedSource(5));
      Collection<Source> sorted = new SortByOrderStage(stage).sources();
      StringBuilder orders = new StringBuilder();
      for (Source source : sorted) {
         orders.append("[" + source.order() + "]");
      }
      assertEquals("[0][1][2][3][4][5][6]", orders.toString());
   }
}
