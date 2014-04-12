/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

/**
 * Things did not go so well in the land of rendering sources
 */
public class SourceException extends RuntimeException {

   public SourceException(String message) {
      super(message);
   }
}
