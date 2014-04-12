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

   public DiskPutTarget(File output) {
      this.output = output;
   }

   private void ensureDirectoryExists(String key) {
      String[] fragments = key.split(File.pathSeparator);
      File dirToMake = output;
      for (int k = 0; k < fragments.length - 1; k++) {
         dirToMake = new File(dirToMake, fragments[k]);
         dirToMake.mkdir();
      }
   }

   @Override
   public void upload(String key, String md5, String contentType, InputStream body, long contentLength) throws Exception {
      ensureDirectoryExists(key);
      File tmp = new File(output, key + ".tmp");
      File destination = new File(output, key);
      System.out.println("write:" + destination.toString());
      FileOutputStream output = new FileOutputStream(tmp);
      try {
         byte[] buffer = new byte[64 * 1024];
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
