/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.jeffrey.web.assemble.DiskPutTarget;
import io.jeffrey.web.assemble.InMemoryAssembler;
import io.jeffrey.web.assemble.PutTarget;
import io.jeffrey.web.assemble.S3PutObjectTarget;
import io.jeffrey.web.stages.*;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main() which is the entry point to the CLI wake tool
 */
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

      ArrayList<String> errors = new ArrayList<>();
      File input = config.getFile(Config.ConfigFile.Input, errors);
      File merge = config.getFile(Config.ConfigFile.Merge, errors);
      File output = null;
      String bucket = config.get(Config.ConfigKey.Bucket, false, errors);
      String redirectBucket = config.get(Config.ConfigKey.RedirectBucket, false, errors);

      AmazonS3 s3 = null;
      if (bucket != null) {
         String accessKey = config.get(Config.ConfigKey.AccessKey, true, errors);
         String secret = config.get(Config.ConfigKey.SecretKey, true, errors);
         AWSCredentials credentials = new BasicAWSCredentials(accessKey, secret);
         s3 = new AmazonS3Client(credentials);
      } else {
         output = config.getFile(Config.ConfigFile.Output, errors);
      }

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

      // build the spelling page
      // TODO: clean this up
      final StringBuilder spelling = new StringBuilder();
      spelling.append("<pre>");
      assembly.validate((url, html) -> {
         try {
            spelling.append("<h1>" + url + "</h1>\n");
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
                     spelling.append(plain);
                     spelling.append("\n");
                     for (RuleMatch match : matches) {
                        spelling.append("Potential error at line " + match.getLine() + ", column " + match.getColumn() + ": " + match.getMessage());
                        spelling.append("\n");
                        spelling.append("Suggested correction: " + match.getSuggestedReplacements());
                        spelling.append("\n");
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
      spelling.append("</pre>");

      // let's simply write it to disk
      PutTarget target;
      if (s3 == null) {
         System.out.println("writing to disk");
         target = new DiskPutTarget(output);
      } else {
         System.out.println("writing to s3");
         target = new S3PutObjectTarget(bucket, s3);
      }
      assembly.put("__spelling.html", spelling.toString());
      // engage!
      assembly.assemble(target);

   }
}
