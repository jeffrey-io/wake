package io.jeffrey.web.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This looks for embedded tags and then tags them inline and then provides a simple complex object to walk them; note, this class will compile the body once constructed
 */
public class TagsFilteredSource extends Source {
   private final static String TAG_REGEX = "&&([^&]*)&&";
   private final Source source;
   private final String compiledBody;
   private final Object tags;

   /**
    * Rewrite tags and count them
    *
    * @param source
    */
   public TagsFilteredSource(Source source) {
      this.source = source;
      final HashMap<String, String> replacements = new HashMap<>();
      String _compiledBody = source.get("body");
      final Matcher matches = Pattern.compile(TAG_REGEX).matcher(_compiledBody);
      final TreeMap<String, HashMap<String, Object>> counts = new TreeMap<>();
      while (matches.find()) {
         String tag = matches.group(1);
         HashMap<String, Object> value = counts.get(tag);
         if (value == null) {
            value = new HashMap<>();
            value.put("tag", tag);
            value.put("count", 0);
            value.put("ticks", new ArrayList<String>());
            counts.put(tag, value);
            replacements.put("&&" + tag + "&&", "<em class=\"tag\">" + tag + "</em>");
         }
         value.put("count", ((int) value.get("count")) + 1);
         ((ArrayList<String>) value.get("ticks")).add(".");
      }
      for (String tag : replacements.keySet()) {
         _compiledBody = _compiledBody.replaceAll(Pattern.quote(tag), replacements.get(tag));
      }
      this.compiledBody = _compiledBody;
      this.tags = new ArrayList<>(counts.values());
   }

   @Override
   public String get(String key) {
      if ("body".equalsIgnoreCase(key)) {
         return compiledBody;
      }
      return source.get(key);
   }

   @Override
   public void populateDomain(Set<String> domain) {
      source.populateDomain(domain);
   }

   @Override
   public void walkComplex(BiConsumer<String, Object> injectComplex) {
      source.walkComplex(injectComplex);
      injectComplex.accept("tags", tags);
   }
}
