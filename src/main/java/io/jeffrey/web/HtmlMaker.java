package io.jeffrey.web;

import io.jeffrey.web.sources.Source;
import io.jeffrey.web.stages.Stage;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class HtmlMaker {
   public File outputPath;

   public HtmlMaker(File outputPath) {
      this.outputPath = outputPath;
   }

   public void make(Stage stage) throws Exception {
      for (Source source : stage.sources()) {
         make(source);
      }
   }

   public void make(Source source) throws Exception {
      String url = source.get("url");
      String body = source.get("body");
      File dest = new File(outputPath, url);
      // TODO: deal with paths
      System.out.println(dest);
      FileWriter writer = new FileWriter(dest);
      writer.write(body);
      writer.flush();
      writer.close();
   }
}
