/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.Source;

import java.util.Collection;
import java.util.Set;

/**
 * Primarily for testing, but this enables taking a bunch of ad-hoc sources and turning them into a stage
 */
public class SetStage extends Stage {

   private final Set<Source> sources;

   public SetStage(final Set<Source> sources) {
      this.sources = sources;
   }

   @Override
   public Collection<Source> sources() {
      return sources;
   }
}
