package io.jeffrey.web.stages;

import io.jeffrey.web.sources.ComplexMapInjectedSource;
import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * This will lazily build an index over all the files
 */
public class CrossBuildIndexStage extends Stage {

  private final Stage                                               prior;

  private final TreeMap<String, ArrayList<HashMap<String, String>>> index;
  private final ArrayList<HashMap<String, String>>                  urls;

  public CrossBuildIndexStage(final Stage prior) {
    this.prior = prior;
    this.index = new TreeMap<>();
    this.urls = new ArrayList<HashMap<String, String>>();
  }

  private void add(final Source source, final HashSet<String> docIndex) {
    final HashMap<String, String> sourceRef = new HashMap<>();
    sourceRef.put("url", source.get("url"));
    sourceRef.put("title", source.get("title"));
    urls.add(sourceRef);

    for (final String keyword : docIndex) {
      ArrayList<HashMap<String, String>> keywordList = index.get(keyword);
      if (keywordList == null) {
        keywordList = new ArrayList<>();
        index.put(keyword, keywordList);
      }
      final HashMap<String, String> item = new HashMap<>();
      item.put("url", source.get("url"));
      item.put("title", source.get("title"));
      keywordList.add(item);
    }
  }

  @Override
  public Collection<Source> sources() {
    final ArrayList<Source> sources = new ArrayList<>();
    for (final Source source : prior.sources()) {
      source.walkComplex((key, o) -> {
        if ("index-by-tags".equals(key)) {
          if (o instanceof HashSet) {
            add(source, (HashSet<String>) o);
          }
        }
      });
    }
    final ArrayList<Object> globalIndex = new ArrayList<>();
    for (final String keyword : index.keySet()) {
      final HashMap<String, Object> keywordSection = new HashMap<>();
      keywordSection.put("name", keyword);
      keywordSection.put("ref", index.get(keyword));
      globalIndex.add(keywordSection);
    }

    for (final Source source : prior.sources()) {
      final Source injectGlobalTagIndex = new ComplexMapInjectedSource(source, "global-tag-index", globalIndex);
      final Source injectUrls = new ComplexMapInjectedSource(injectGlobalTagIndex, "global-url-index", urls);
      sources.add(injectUrls);
    }
    return sources;
  }
}
