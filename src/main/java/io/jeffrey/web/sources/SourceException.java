/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

/**
 * Things did not go so well in the land of rendering sources
 */
public class SourceException extends RuntimeException {
  private static final long serialVersionUID = 921111150445411032L;

  public SourceException(final String message) {
    super(message);
  }
}
