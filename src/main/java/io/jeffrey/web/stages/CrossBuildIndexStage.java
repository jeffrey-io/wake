package io.jeffrey.web.stages;

import io.jeffrey.web.sources.ComplexMapInjectedSource;
import io.jeffrey.web.sources.Source;

import java.util.*;

/**
 * This will lazily build an index over all the files
 */
public class CrossBuildIndexStage extends Stage {

   private final Stage prior;

   private final TreeMap<String, ArrayList<HashMap<String, String>>> index;

   public CrossBuildIndexStage(Stage prior) {
      this.prior = prior;
      this.index = new TreeMap<>();
   }

   private void add(Source source, HashSet<String> docIndex) {
      for (String keyword : docIndex) {
         ArrayList<HashMap<String, String>> keywordList = index.get(keyword);
         if (keywordList == null) {
            keywordList = new ArrayList<>();
            index.put(keyword, keywordList);
         }
         HashMap<String, String> item = new HashMap<>();
         item.put("url", source.get("url"));
         item.put("title", source.get("title"));
         keywordList.add(item);
      }
   }

   @Override
   public Collection<Source> sources() {
      ArrayList<Source> sources = new ArrayList<>();
      for (Source source : prior.sources()) {
         source.walkComplex((key, o) -> {
            if ("index-by-tags".equals(key)) {
               if (o instanceof HashSet) {
                  add(source, (HashSet<String>) o);
               }
            }
         });
      }
      ArrayList<Object> globalIndex = new ArrayList<>();
      for (String keyword : index.keySet()) {
         HashMap<String, Object> keywordSection = new HashMap<>();
         keywordSection.put("name", keyword);
         keywordSection.put("ref", index.get(keyword));
         globalIndex.add(keywordSection);
      }

      for (Source source : prior.sources()) {
         sources.add(new ComplexMapInjectedSource(source, "global-tag-index", globalIndex));
      }
      return sources;
   }
}
