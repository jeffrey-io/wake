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

  public enum ConfigFile {
    Input("input", true, false), Merge("merge", true, false), Output("output", true, false);

    private final String  key;
    private final boolean requireDirectory;
    private final boolean requireFile;

    private ConfigFile(final String key, final boolean requireDirectory, final boolean requireFile) {
      this.key = key;
      this.requireDirectory = requireDirectory;
      this.requireFile = requireFile;
    }
  }

  public enum ConfigKey {
    Bucket("bucket"), RedirectBucket("redirect-bucket"), AccessKey("akid"), SecretKey("secret");

    private final String key;

    private ConfigKey(final String key) {
      this.key = key;
    }
  }

  private static class ConfigValue {
    public final String value;
    public final int    lineNo;

    public ConfigValue(final String value, final int lineNo) {
      this.value = value;
      this.lineNo = lineNo;
    }
  }

  private final HashMap<String, ConfigValue> values;

  public Config() {
    this.values = new HashMap<>();

    // defaults
    this.values.put("input", new ConfigValue("in", -1));
    this.values.put("merge", new ConfigValue("merge", -1));
    this.values.put("output", new ConfigValue("out", -1));
  }

  public Config(final Reader rawReader) throws IOException {
    this();
    final BufferedReader reader = new BufferedReader(rawReader);
    String ln;
    int lineNo = 0;
    while ((ln = reader.readLine()) != null) {
      lineNo++;
      ln = ln.trim();
      if (ln.length() == 0) {
        continue;
      }
      if (ln.charAt(0) == '#') {
        continue;
      }
      final int kEq = ln.indexOf('=');
      if (kEq < 0) {
        continue;
      }
      final String key = ln.substring(0, kEq).trim().toLowerCase();
      if (key.length() == 0) {
        continue;
      }
      final String val = ln.substring(kEq + 1).trim();
      values.put(key, new ConfigValue(val, lineNo));
    }
  }

  public String get(final ConfigKey key, final boolean required, final ArrayList<String> errors) {
    if (required) {
      return validateMustExist(key.key, errors).value;
    }
    final ConfigValue value = values.get(key.key);
    if (value == null) {
      return null;
    }
    return value.value;
  }

  public File getFile(final ConfigFile file, final ArrayList<String> errors) {
    return validateMustBeFileThatExists(file.key, file.requireDirectory, file.requireFile, errors);
  }

  private File validateMustBeFileThatExists(final String key, final boolean requireDirectory, final boolean requireFile, final ArrayList<String> errors) {
    final ConfigValue configFilename = validateMustExist(key, errors);
    if (configFilename == null) {
      return null;
    }
    final File file = new File(configFilename.value);
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

  private ConfigValue validateMustExist(final String key, final ArrayList<String> errors) {
    final ConfigValue configValue = values.get(key);
    if (configValue == null) {
      errors.add("'" + key + "' must be present");
      return null;
    }
    return configValue;
  }
}
