package io.jeffrey.web;

import io.jeffrey.web.assemble.DiskPutTarget;
import io.jeffrey.web.assemble.InMemoryAssembler;
import io.jeffrey.web.assemble.PutTarget;
import io.jeffrey.web.stages.*;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
      if (configFile.exists()) {
         FileReader reader = new FileReader(configFile);
         try {
            config = new Config(reader);
         } finally {
            reader.close();
            ;
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
      // inject the topology (connect the pages together according to the one true tree)
      InjectTopologyStage withTopology = new InjectTopologyStage(sorted);
      // inject snippets
      SnippetInjectorStage withSnippets = new SnippetInjectorStage(withTopology);
      // put templates into place
      TemplateCrossStage withTemplates = new TemplateCrossStage(withSnippets);
      // assemble the manifest
      InMemoryAssembler assembly = new InMemoryAssembler(merge, withTemplates);

      assembly.validate((url, html) -> {
         try {
            JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
            langTool.activateDefaultPatternRules();
            Document doc = Jsoup.parse(html);
            Elements elements = doc.select("p");
            elements.forEach((element) -> {
               String plain = new HtmlToPlainText().getPlainText(element);
               plain = plain.replaceAll(" \\s*", " ");
               plain = plain.replaceAll("\\s* ", " ");
               try {
                  List<RuleMatch> matches = langTool.check(plain);
                  if (matches.size() > 0) {
                     System.out.println(plain);
                     for (RuleMatch match : matches) {
                        System.out.println("Potential error at line " + match.getLine() + ", column " + match.getColumn() + ": " + match.getMessage());
                        System.out.println("Suggested correction: " + match.getSuggestedReplacements());
                     }
                  }
               } catch (IOException ioe) {
                  throw new RuntimeException(ioe);
               }
            });
         } catch (Exception err) {
            System.err.println("Exception checking:" + url);
            err.printStackTrace();
         }
      });
      // let's simply write it to disk
      PutTarget target = new DiskPutTarget(output);
      // engage!
      assembly.assemble(target);
   }
}
