/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * This will replace site links of the form |>$name| with real links
 */
public class LinkageSource extends Source {

  private final Source                  source;
  private final HashMap<String, Source> links;

  public LinkageSource(final Source source, final HashMap<String, Source> links) {
    this.source = source;
    this.links = links;
  }

  @Override
  public String get(final String key) {
    if ("body".equals(key)) {
      String body = source.get(key);
      for (final Entry<String, Source> e : links.entrySet()) {
        final String search = Pattern.quote("|>" + e.getKey() + "|");
        final String replacement = "<a href=\"/" + e.getValue().get("url") + "\">" + e.getValue().get("title") + "</a>";
        body = body.replaceAll(search, replacement);
      }
      return body;
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
  }

}
