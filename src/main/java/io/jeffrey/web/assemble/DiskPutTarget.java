/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.assemble;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Implements a disk backed 'PutTarget'
 */
public class DiskPutTarget implements PutTarget {

  private final File output;

  public DiskPutTarget(final File output) {
    this.output = output;
  }

  private void ensureDirectoryExists(final String key) {
    final String[] fragments = key.split(File.pathSeparator);
    File dirToMake = output;
    for (int k = 0; k < fragments.length - 1; k++) {
      dirToMake = new File(dirToMake, fragments[k]);
      dirToMake.mkdir();
    }
  }

  @Override
  public void upload(final String key, final String md5, final String contentType, final InputStream body, final long contentLength) throws Exception {
    ensureDirectoryExists(key);
    final File tmp = new File(output, key + ".tmp");
    final File destination = new File(output, key);
    System.out.println("write:" + destination.toString());
    final FileOutputStream output = new FileOutputStream(tmp);
    try {
      final byte[] buffer = new byte[64 * 1024];
      int rd;
      while ((rd = body.read(buffer)) > 0) {
        output.write(buffer, 0, rd);
      }
    } finally {
      output.close();
    }
    tmp.renameTo(destination);
  }
}
