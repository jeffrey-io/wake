package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;
import org.junit.Test;

/**
 * Created by jeffrey on 4/13/14.
 */
public class TableOfContentsSourceTest extends TestingBase {

   @Test
   public void testAnchorInjection() {
      HashMapSource src = createVerySimpleSource();
      src.put("body", "<h1>Test</h1>    {<h2>hi</h2>} {<h1>low -be <s>chill!</s><b>hi</b></h1>} {<h2>a</h2>} {<h2>b</h2>} <h1>finally</h1> ");
      TableOfContentsSource toc = new TableOfContentsSource(src);
      String expected = "<a name=\"Test\"></a><h1>Test</h1>    {<a name=\"hi\"></a><h2>hi</h2>} {<a name=\"low_be_chill_hi\"></a><h1>low -be <s>chill!</s><b>hi</b></h1>} {<a name=\"a\"></a><h2>a</h2>} {<a name=\"b\"></a><h2>b</h2>} <a name=\"finally\"></a><h1>finally</h1> ";
      assertBodyEvaluate(toc, expected);
   }

   @Test
   public void testTOC() {
      HashMapSource src = createVerySimpleSource();
      src.put("body", "<h1>Test</h1>    {<h2>hi</h2>} {<h1>low -be <s>chill!</s><b>hi</b></h1>} {<h2>a</h2>} {<h2>b</h2>} <h1>finally</h1> <h2> ninja</h2> <h2> pirate</h2> <h5>faux</h5> ");
      TableOfContentsSource toc = new TableOfContentsSource(src);

      HashMapSource template = createVerySimpleSource();
      template.put("body", "{{#toc}}[{{title}}:{{name}}]{{/toc}}");
      ApplyTemplateBodySource fin = new ApplyTemplateBodySource(toc, template);

      assertBodyEvaluate(fin, "[Test:Test][low -be chill!hi:low_be_chill_hi][finally:finally]");

      template.put("body", "{{#toc}}[{{title}}:{{name}}]<ul>{{#children}}[{{title}}:{{name}}]{{/children}}</ul>{{/toc}}");
      assertBodyEvaluate(fin, "[Test:Test]<ul>[hi:hi]</ul>[low -be chill!hi:low_be_chill_hi]<ul>[a:a][b:b]</ul>[finally:finally]<ul>[ninja:ninja][pirate:pirate]</ul>");

   }
}
