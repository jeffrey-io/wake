package io.jeffrey.web.stages;

import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 3/19/2014.
 */
public class SortByOrderStage extends Stage {
  private final Stage priorStage;

  public SortByOrderStage(Stage priorStage) {
    this.priorStage = priorStage;
  }

  @Override
  public Collection<Source> sources() {
    ArrayList<Source> sources = new ArrayList<>();
    sources.addAll(priorStage.sources());
    Collections.sort(sources, Comparator.comparingLong((item) -> item.order()));
    return sources;
  }
}
