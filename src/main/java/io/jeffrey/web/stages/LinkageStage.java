/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.LinkageSource;
import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * This will cross all the pages together, and enable linking between final pages
 */
public class LinkageStage extends Stage {

  private final Stage stage;

  public LinkageStage(final Stage stage) {
    this.stage = stage;
  }

  @Override
  public Collection<Source> sources() {
    final ArrayList<Source> next = new ArrayList<>();
    final HashMap<String, Source> links = new HashMap<>();
    for (final Source src : stage.sources()) {
      final String name = src.get("name");
      if (name != null) {
        links.put(name, src);
      }
      // note, this is all lazily linked, so the links
      // should not be acted upon at this stage
      next.add(new LinkageSource(src, links));
    }
    return next;
  }

}
