/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This defines the configuration file parser and a way to place contractual obligations on the various things in the config
 */
public class Config {

   private static class ConfigValue {
      public final String key;
      public final String value;
      public final int lineNo;

      public ConfigValue(String key, String value, int lineNo) {
         this.key = key;
         this.value = value;
         this.lineNo = lineNo;
      }
   }

   private final HashMap<String, ConfigValue> values;

   public Config() {
      this.values = new HashMap<>();

      // defaults
      this.values.put("input", new ConfigValue("input", "in", -1));
      this.values.put("merge", new ConfigValue("merge", "merge", -1));
      this.values.put("output", new ConfigValue("output", "out", -1));
   }

   public Config(Reader rawReader) throws IOException {
      this();
      BufferedReader reader = new BufferedReader(rawReader);
      String ln;
      int lineNo = 0;
      while ((ln = reader.readLine()) != null) {
         lineNo++;
         ln = ln.trim();
         if (ln.length() == 0)
            continue;
         if (ln.charAt(0) == '#')
            continue;
         int kEq = ln.indexOf('=');
         if (kEq < 0)
            continue;
         String key = ln.substring(0, kEq).trim().toLowerCase();
         if (key.length() == 0)
            continue;
         String val = ln.substring(kEq + 1).trim();
         values.put(key, new ConfigValue(key, val, lineNo));
      }
   }

   private ConfigValue validateMustExist(String key, ArrayList<String> errors) {
      ConfigValue configValue = values.get(key);
      if (configValue == null) {
         errors.add("'" + key + "' must be present");
         return null;
      }
      return configValue;
   }

   private File validateMustBeFileThatExists(String key, boolean requireDirectory, boolean requireFile, ArrayList<String> errors) {
      ConfigValue configFilename = validateMustExist(key, errors);
      if (configFilename == null) {
         return null;
      }
      File file = new File(configFilename.value);
      if (!file.exists()) {
         errors.add("'" + configFilename.value + "' must be exist in the file system (line " + configFilename.lineNo + ")");
         return null;
      }
      if (requireDirectory && !file.isDirectory()) {
         errors.add("'" + configFilename.value + "' must be a directory (line " + configFilename.lineNo + ")");
         return null;
      }
      if (requireFile && file.isDirectory()) {
         errors.add("'" + configFilename.value + "' must be a file (line " + configFilename.lineNo + ")");
         return null;
      }
      return file;
   }

   public enum ConfigKey {
      Bucket("bucket"),
      RedirectBucket("redirect-bucket"),
      AccessKey("akid"),
      SecretKey("secret");

      private final String key;

      private ConfigKey(final String key) {
         this.key = key;
      }
   }

   public enum ConfigFile {
      Input("input", true, false),
      Merge("merge", true, false),
      Output("output", true, false);

      private final String key;
      private final boolean requireDirectory;
      private final boolean requireFile;

      private ConfigFile(String key, boolean requireDirectory, boolean requireFile) {
         this.key = key;
         this.requireDirectory = requireDirectory;
         this.requireFile = requireFile;
      }
   }

   public File getFile(ConfigFile file, ArrayList<String> errors) {
      return validateMustBeFileThatExists(file.key, file.requireDirectory, file.requireFile, errors);
   }

   public String get(ConfigKey key, boolean required, ArrayList<String> errors) {
      if (required) {
         return validateMustExist(key.key, errors).value;
      }
      ConfigValue value = values.get(key.key);
      if (value == null) {
         return null;
      }
      return value.value;
   }
}
