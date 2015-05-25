/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.assemble;

import java.io.InputStream;

/**
 * Defines how keys and values get put; this interface is generic enough to think about putting to both a disk and to something like S3
 */
public interface PutTarget {

  /**
   * upload the file
   */
  public void upload(String key, String md5, String contentType, InputStream body, long contentLength) throws Exception;
}
