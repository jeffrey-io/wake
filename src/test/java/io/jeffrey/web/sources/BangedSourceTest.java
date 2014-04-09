package io.jeffrey.web.sources;

import io.jeffrey.web.TestingBase;

import java.io.Reader;

/**
 * Created by jeffrey on 4/9/14.
 */
public class BangedSourceTest extends TestingBase {

   public void testBangParsing() throws Exception {
      Reader reader = readerize("#!key=value\nbody");
      BangedSource source = new BangedSource("filename.txt", reader);
      assertEvaluate("key", source, "value");
      assertEvaluate("body", source, "body\n");
      assertEvaluate("name", source, "filename");
      assertEvaluate("url", source, "filename.html");
      assertItemization(source, "key", "body", "name", "url");
   }

   public void testOverrideDefaultNameAndUrlPossible() throws Exception {
      Reader reader = readerize("#!key=value\nbody\n#!name=fooness\n#!url=some-faux-folder");
      BangedSource source = new BangedSource("filename.txt", reader);
      assertEvaluate("key", source, "value");
      assertEvaluate("body", source, "body\n");
      assertEvaluate("name", source, "fooness");
      assertEvaluate("url", source, "some-faux-folder");
      assertItemization(source, "key", "body", "name", "url");
   }
}
