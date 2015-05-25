/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This takes a file and then parses out the she-bangs (#!) which are used for injecting key value pairs
 * <p>
 * TODO: factor into "ReaderSource" and do the line by line analysis here
 */
public class BangedSource extends Source {
   private final HashMap<String, String> values;
   private int currentLineNumber;

   /**
    * construct
    *
    * @param filename the name of the source
    * @param input    the reader that provides the source
    * @throws IOException
    */
   public BangedSource(String filename, Reader input) throws IOException {
      int lastDotK = filename.lastIndexOf('.');
      String ext = filename.substring(lastDotK + 1);
      String name = filename.substring(0, lastDotK);
      this.values = new HashMap<>();
      values.put("name", name);
      values.put("url", name + ".html");
      values.put("ext", ext);
      BufferedReader reader = new BufferedReader(input);
      try {
         final StringBuilder body = new StringBuilder();
         currentLineNumber = 0;
         reader.lines().forEach((ln) -> {
            currentLineNumber++;
            if (ln.startsWith("#!")) {
               indexBang(ln.substring(2).trim());
            } else {
               body.append(ln);
               body.append("\n");
            }
         });
         values.put("body", body.toString());
      } finally {
         reader.close();
      }
   }

   /**
    * take a line of the form A=B and then inject the value 'B' at key 'A'
    *
    * @param assignmentRaw
    */
   private void indexBang(String assignmentRaw) {
      int kEq = assignmentRaw.indexOf('=');
      if (kEq < 0)
         throw new SourceException("there should be an '=' in '" + assignmentRaw + "' on line " + currentLineNumber);
      String key = assignmentRaw.substring(0, kEq).trim().toLowerCase();
      if (key.length() == 0)
         throw new SourceException("there should be at least one character before the '=' in '" + assignmentRaw + "' on line " + currentLineNumber);
      String val = assignmentRaw.substring(kEq + 1).trim();
      values.put(key, val);
   }

   @Override
   public String get(String key) {
      return values.get(key);
   }

   @Override
   public void populateDomain(Set<String> domain) {
      domain.addAll(values.keySet());
   }

   @Override
   public void walkComplex(BiConsumer<String, Object> injectComplex) {
      // nothing complicated about it
   }
}
