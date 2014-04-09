package io.jeffrey.web;

import io.jeffrey.web.stages.*;

public class Wake {
  public static void main(String[] args) throws Exception {
    // write your code here
    String input = "C:\\Users\\jeffrey\\Dropbox\\next.jeffrey.io\\input";
    String merge = "C:\\Users\\jeffrey\\Dropbox\\next.jeffrey.io\\merge";
    String output = "C:\\Users\\jeffrey\\Dropbox\\next.jeffrey.io\\out";
    // load them from disk
    DiskLoaderStage raw = new DiskLoaderStage(input);
    // sort them for giggles
    SortByOrderStage sorted = new SortByOrderStage(raw);
    // inject the topology (connect the pages together in seemingly random ways)
    TopologizeStage withTopology = new TopologizeStage(sorted);
    // inject snippets
    SnippetInjector withSnippets = new SnippetInjector(withTopology);
    // put templates into place
    TemplateCrossStage withTemplates = new TemplateCrossStage(withSnippets);
    // remove snippets
    SnippetInjector filterOutSnippets = new SnippetInjector(withTemplates);
    HtmlMaker maker = new HtmlMaker(output);
    maker.make(filterOutSnippets);
  }
}
