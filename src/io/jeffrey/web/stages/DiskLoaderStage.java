package io.jeffrey.web.stages;

import io.jeffrey.web.sources.BangedSource;
import io.jeffrey.web.sources.MarkdownFilteredSource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.TagDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class DiskLoaderStage extends Stage {
  private final Collection<Source> sources;

  public DiskLoaderStage(String inputPath) {
    ArrayList<Source> _sources = new ArrayList<>();
    File inputPathRoot = new File(inputPath);
    for (File inputFile : inputPathRoot.listFiles()) {
      Source source = loadIfPossible(inputFile);
      if (source == null) {
        System.err.println("Did not understand the source:" + inputFile.toString());
      } else {
        _sources.add(source);
      }
    }
    this.sources = Collections.unmodifiableCollection(_sources);
  }

  private Source loadIfPossible(File inputFile) {
    String name = inputFile.getName();
    if (!(name.endsWith(".html") || name.endsWith(".markdown"))) {
      return null;
    }
    try {
      Source source = new TagDetector(new BangedSource(inputFile));
      if (name.endsWith(".markdown")) {
        source =  new MarkdownFilteredSource(source, "body");
      }
      return source;
    } catch (Exception err) {
      System.err.println("Skipping:" + inputFile);
      err.printStackTrace();
      return null;
    }
  }

  @Override
  public Collection<Source> sources() {
    return sources;
  }
}
