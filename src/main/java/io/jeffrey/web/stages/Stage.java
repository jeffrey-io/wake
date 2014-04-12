/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.Source;

import java.util.Collection;

/**
 * Defines a stage of the site generation
 */
public abstract class Stage {

   public abstract Collection<Source> sources();
}
