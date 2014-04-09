package io.jeffrey.web;

import io.jeffrey.web.stages.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Wake {
   public static void main(String[] args) throws Exception {
      String configFilename = ".wake";
      for (int k = 0; k < args.length - 1; k++) {
         if ("--config".equals(args[k]) || "-c".equals(args[k])) {
            configFilename = args[k + 1];
         }
      }

      File configFile = new File(configFilename);
      Config config;
      if( configFile.exists()) {
         FileReader reader = new FileReader(configFile);
         try {
            config = new Config(reader);
         } finally {
            reader.close();;
         }
      } else {
         // roll with the defaults (thus, no deployment
         config = new Config();
      }

      // write your code here
      ArrayList<String> errors = new ArrayList<>();

      File input = config.getFile(Config.ConfigFile.Input, errors);
      File merge = config.getFile(Config.ConfigFile.Merge, errors);
      File output = config.getFile(Config.ConfigFile.Output, errors);

      if (errors.size() > 0) {
         System.err.println("There were too many errors:");
         errors.forEach((error) -> System.err.println(error));
         return;
      }
      // load them from disk
      DiskLoaderStage raw = new DiskLoaderStage(input);
      // sort them for giggles
      SortByOrderStage sorted = new SortByOrderStage(raw);
      // inject the topology (connect the pages together in seemingly random ways)
      InjectTopologyStage withTopology = new InjectTopologyStage(sorted);
      // inject snippets
      SnippetInjectorStage withSnippets = new SnippetInjectorStage(withTopology);
      // put templates into place
      TemplateCrossStage withTemplates = new TemplateCrossStage(withSnippets);
      // remove snippets
      SnippetInjectorStage filterOutSnippets = new SnippetInjectorStage(withTemplates);
      HtmlMaker maker = new HtmlMaker(output);
      maker.make(filterOutSnippets);
   }
}
