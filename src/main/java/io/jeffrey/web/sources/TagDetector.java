package io.jeffrey.web.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 3/19/2014.
 */
public class TagDetector extends Source {
  private final static String TAG_REGEX = "&&([^&]*)&&";
  private final Source source;
  private final Object tags;
  private final HashMap<String, String> replacements = new HashMap<>();

  public TagDetector(Source source) {
    this.source = source;
    Matcher matches = Pattern.compile(TAG_REGEX).matcher(source.get("body"));
    TreeMap<String, HashMap<String, Object>> counts = new TreeMap<>();
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
    this.tags = new ArrayList<>(counts.values());
  }

  @Override
  public String get(String key) {
    if ("body".equalsIgnoreCase(key)) {
      String body = source.get("body");
      for (String tag : replacements.keySet()) {
        body = body.replaceAll(Pattern.quote(tag), replacements.get(tag));
      }
      return body;
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
