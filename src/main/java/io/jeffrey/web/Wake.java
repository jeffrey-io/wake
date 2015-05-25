/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web;

import io.jeffrey.web.analysis.LanguageToolMap;
import io.jeffrey.web.assemble.DiskPutTarget;
import io.jeffrey.web.assemble.InMemoryAssembler;
import io.jeffrey.web.assemble.PutTarget;
import io.jeffrey.web.assemble.S3PutObjectTarget;
import io.jeffrey.web.stages.CompressHTMLStage;
import io.jeffrey.web.stages.CrossBuildIndexStage;
import io.jeffrey.web.stages.DiskLoaderStage;
import io.jeffrey.web.stages.InjectTopologyStage;
import io.jeffrey.web.stages.LinkageStage;
import io.jeffrey.web.stages.SnippetInjectorStage;
import io.jeffrey.web.stages.SortByOrderStage;
import io.jeffrey.web.stages.TemplateCrossStage;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.languagetool.language.AmericanEnglish;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * The main() which is the entry point to the CLI wake tool
 */
public class Wake {
  public static void main(final String[] args) throws Exception {
    String configFilename = ".wake";
    for (int k = 0; k < args.length - 1; k++) {
      if ("--config".equals(args[k]) || "-c".equals(args[k])) {
        configFilename = args[k + 1];
      }
    }

    final File configFile = new File(configFilename);
    Config config;
    if (configFile.exists()) {
      final FileReader reader = new FileReader(configFile);
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

    final ArrayList<String> errors = new ArrayList<>();
    final File input = config.getFile(Config.ConfigFile.Input, errors);
    final File merge = config.getFile(Config.ConfigFile.Merge, errors);
    File output = null;
    final String bucket = config.get(Config.ConfigKey.Bucket, false, errors);
    config.get(Config.ConfigKey.RedirectBucket, false, errors);

    AmazonS3 s3 = null;
    if (bucket != null) {
      final String accessKey = config.get(Config.ConfigKey.AccessKey, true, errors);
      final String secret = config.get(Config.ConfigKey.SecretKey, true, errors);
      final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secret);
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
    final DiskLoaderStage raw = new DiskLoaderStage(input);
    // sort them for giggles
    final SortByOrderStage sorted = new SortByOrderStage(raw);
    // inject a global index
    final CrossBuildIndexStage indexed = new CrossBuildIndexStage(sorted);
    // inject the topology (connect the pages together according to the one true tree)
    final InjectTopologyStage withTopology = new InjectTopologyStage(indexed);
    // inject snippets
    final SnippetInjectorStage withSnippets = new SnippetInjectorStage(withTopology);
    // put templates into place
    final TemplateCrossStage withTemplates = new TemplateCrossStage(withSnippets);
    // enable linkage between pages
    final LinkageStage linked = new LinkageStage(withTemplates);
    // clean up the HTML
    final CompressHTMLStage clean = new CompressHTMLStage(linked);
    // assemble the manifest
    final InMemoryAssembler assembly = new InMemoryAssembler(merge, clean, "http://" + bucket + "/");

    // perform a bit of analysis
    final String spelling = LanguageToolMap.checkAssemblyAndReport(assembly, new AmericanEnglish());

    // let's simply write it to disk
    PutTarget target;
    if (s3 == null) {
      System.out.println("writing to disk");
      target = new DiskPutTarget(output);
    } else {
      System.out.println("writing to s3");
      target = new S3PutObjectTarget(bucket, s3);
    }
    assembly.put("__spelling.html", spelling);
    // engage!
    assembly.assemble(target);

  }
}
