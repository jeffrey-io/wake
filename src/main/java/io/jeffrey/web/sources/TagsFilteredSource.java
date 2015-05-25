/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
  private final Source        source;
  private final String        compiledBody;
  private final Object        tags;
  public final Set<String>    index;

  /**
   * Rewrite tags and count them
   *
   * @param source
   */
  public TagsFilteredSource(final Source source) {
    this.source = source;
    final HashMap<String, String> replacements = new HashMap<>();
    String _compiledBody = source.get("body");
    final Matcher matches = Pattern.compile(TAG_REGEX).matcher(_compiledBody);
    final TreeMap<String, HashMap<String, Object>> counts = new TreeMap<>();
    final TreeMap<String, ArrayList<String>> ticks = new TreeMap<>();
    
    this.index = new HashSet<>();
    while (matches.find()) {
      final String tag = matches.group(1);
      HashMap<String, Object> value = counts.get(tag);
      if (value == null) {
        value = new HashMap<>();
        value.put("tag", tag);
        index.add(tag);
        value.put("count", 0);
        ArrayList<String> myticks = new ArrayList<String>();
        value.put("ticks", myticks);
        ticks.put(tag, myticks);
        counts.put(tag, value);
        replacements.put("&&" + tag + "&&", "<em class=\"tag\">" + tag + "</em>");
      }
      value.put("count", (int) value.get("count") + 1);
      ticks.get(tag).add(".");
    }
    for (final String tag : replacements.keySet()) {
      _compiledBody = _compiledBody.replaceAll(Pattern.quote(tag), replacements.get(tag));
    }
    this.compiledBody = _compiledBody;
    this.tags = new ArrayList<>(counts.values());
  }

  @Override
  public String get(final String key) {
    if ("body".equalsIgnoreCase(key)) {
      return compiledBody;
    }
    return source.get(key);
  }

  @Override
  public void populateDomain(final Set<String> domain) {
    source.populateDomain(domain);
  }

  @Override
  public void walkComplex(final BiConsumer<String, Object> injectComplex) {
    source.walkComplex(injectComplex);
    injectComplex.accept("tags", tags);
    injectComplex.accept("index-by-tags", index);
  }
}
