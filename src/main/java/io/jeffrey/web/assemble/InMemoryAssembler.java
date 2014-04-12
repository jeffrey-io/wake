/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.assemble;

import io.jeffrey.web.sources.Source;
import io.jeffrey.web.stages.Stage;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * This will render the web site and describe a 'TODO' list of things to merge in
 */
public class InMemoryAssembler {

   private static String getContentType(File source, String key) {
      String[] parts = key.split(Pattern.quote("."));
      if (parts.length > 0) {
         String ext = parts[parts.length - 1].toLowerCase();
         if ("png".equals(ext)) return "image/png";
         if ("css".equals(ext)) return "text/css";
      }
      return new MimetypesFileTypeMap().getContentType(source);
   }

   private static class MergeStep {
      private final File source;
      private final long length;
      private final String key;
      private final String md5;
      private final String contentType;

      public MergeStep(File source, String key) {
         this.source = source;
         this.length = source.length();
         this.key = key;
         this.contentType = getContentType(source, key);
         try {
            InputStream input = new FileInputStream(source);
            try {
               byte[] buffer = new byte[64 * 1024];
               int rd;
               MessageDigest digest = MessageDigest.getInstance("MD5");
               while ((rd = input.read(buffer)) > 0) {
                  digest.update(buffer, 0, rd);
               }
               this.md5 = new String(Hex.encodeHex(digest.digest()));
            } finally {
               input.close();
            }
         } catch (Exception notFound) {
            throw new RuntimeException(notFound);
         }
      }
   }

   private ArrayList<MergeStep> merge;
   private HashMap<String, String> html;

   public InMemoryAssembler(File mergePath, Stage stage) {
      this.merge = new ArrayList<>();
      this.html = new HashMap<>();
      registerStage(stage);
      registerMerge(mergePath, mergePath);
   }

   private void registerMerge(File mergePath, File root) {
      String rootBase = root.getPath();
      for (File toMerge : mergePath.listFiles()) {
         if (toMerge.isDirectory()) {
            registerMerge(toMerge, root);
         } else {
            String key = toMerge.getPath().substring(rootBase.length() + 1);
            if (!html.containsKey(key)) {
               merge.add(new MergeStep(toMerge, key));
            } else {
               // TODO: here would be a good place to LOG
            }
         }
      }
   }

   private void registerStage(Stage stage) {
      for (Source source : stage.sources()) {
         String url = source.get("url");
         String body = source.get("body");
         html.put(url, body);
      }
   }

   public void put(String url, String body) {
      html.put(url, body);
   }

   public void validate(BiConsumer<String, String> checker) {
      html.forEach(checker);
   }

   public void assemble(PutTarget target) throws Exception {
      for (MergeStep step : merge) {
         FileInputStream input = new FileInputStream(step.source);
         try {
            target.upload(step.key, step.md5, step.contentType, input, step.length);
         } finally {
            input.close();
         }
      }
      for (String url : html.keySet()) {
         byte[] value = html.get(url).getBytes("UTF-8");
         target.upload(url, DigestUtils.md5Hex(value), "text/html", new ByteArrayInputStream(value), value.length);
      }
   }
}
