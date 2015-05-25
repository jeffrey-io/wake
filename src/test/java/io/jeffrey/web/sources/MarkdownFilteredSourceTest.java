package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;
import org.junit.Test;

/**
 * Created by jeffrey on 4/9/14.
 */
public class MarkdownFilteredSourceTest extends TestingBase {

  @Test
  public void testHtmlProduction() {
     String body = "ninja\n# header #\n ## header2 ##\n* a\n* b";
     HashMapSource data = createVerySimpleSource();
     data.put("body", body);
     MarkdownFilteredSource filtered = new MarkdownFilteredSource(data, "body");
     String expected = "<p>ninja</p>\n" +
           "<h1>header</h1>\n" +
           "<h2>header2</h2>\n" +
           "<ul>\n" +
           "<li>a</li>\n" +
           "<li>b</li>\n" +
           "</ul>\n";
     assertBodyEvaluate(filtered, expected);
     assertItemization(filtered, "body");
  }

  @Test
  public void testLinks() {
     String body = "[J](http://jeffrey.io)";
     HashMapSource data = createVerySimpleSource();
     data.put("body", body);
     MarkdownFilteredSource filtered = new MarkdownFilteredSource(data, "body");
     String expected = "<p><a href=\"http://jeffrey.io\">J</a></p>\n";
     assertBodyEvaluate(filtered, expected);
     assertItemization(filtered, "body");
  }

   @Test
   public void testSkipNonFilteredKey() {
      String body = "ninja\n# header #\n ## header2 ##\n* a\n* b";
      HashMapSource data = createVerySimpleSource();
      data.put("body", body);
      data.put("not", body);
      MarkdownFilteredSource filtered = new MarkdownFilteredSource(data, "body");
      assertEvaluate("not", filtered, body);
      assertItemization(filtered, "body", "not");
   }
}
