/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.assemble;

import io.jeffrey.web.sources.Source;
import io.jeffrey.web.stages.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * This will render the web site and describe a 'TODO' list of things to merge in
 */
public class InMemoryAssembler {

  private static class MergeStep {
    private final File   source;
    private final long   length;
    private final String key;
    private final String md5;
    private final String contentType;

    public MergeStep(final File source, final String key) {
      this.source = source;
      this.length = source.length();
      this.key = key;
      this.contentType = getContentType(source, key);
      try {
        final InputStream input = new FileInputStream(source);
        try {
          final byte[] buffer = new byte[64 * 1024];
          int rd;
          final MessageDigest digest = MessageDigest.getInstance("MD5");
          while ((rd = input.read(buffer)) > 0) {
            digest.update(buffer, 0, rd);
          }
          this.md5 = new String(Hex.encodeHex(digest.digest()));
        } finally {
          input.close();
        }
      } catch (final Exception notFound) {
        throw new RuntimeException(notFound);
      }
    }
  }

  private static String getContentType(final File source, final String key) {
    final String[] parts = key.split(Pattern.quote("."));
    if (parts.length > 0) {
      final String ext = parts[parts.length - 1].toLowerCase();
      if ("png".equals(ext)) {
        return "image/png";
      }
      if ("css".equals(ext)) {
        return "text/css";
      }
    }
    return new MimetypesFileTypeMap().getContentType(source);
  }

  private final ArrayList<MergeStep>    merge;
  private final HashMap<String, String> html;
  private final StringBuilder           audit;

  public InMemoryAssembler(final File mergePath, final Stage stage) {
    this.merge = new ArrayList<>();
    this.html = new HashMap<>();
    this.audit = new StringBuilder();
    this.audit.append("<table>");
    registerStage(stage);
    registerMerge(mergePath, mergePath);
  }

  public void assemble(final PutTarget target) throws Exception {
    for (final MergeStep step : merge) {
      final FileInputStream input = new FileInputStream(step.source);
      try {
        target.upload(step.key, step.md5, step.contentType, input, step.length);
      } finally {
        input.close();
      }
    }
    for (final String url : html.keySet()) {
      final byte[] value = html.get(url).getBytes("UTF-8");
      target.upload(url, DigestUtils.md5Hex(value), "text/html", new ByteArrayInputStream(value), value.length);
    }
    final byte[] value = audit.toString().getBytes("UTF-8");
    target.upload("__audit.html", DigestUtils.md5Hex(value), "text/html", new ByteArrayInputStream(value), value.length);

  }

  public void put(final String url, final String body) {
    html.put(url, body);
  }

  private void registerMerge(final File mergePath, final File root) {
    final String rootBase = root.getPath();
    for (final File toMerge : mergePath.listFiles()) {
      if (toMerge.isDirectory()) {
        registerMerge(toMerge, root);
      } else {
        final String key = toMerge.getPath().substring(rootBase.length() + 1);
        if (!html.containsKey(key)) {
          merge.add(new MergeStep(toMerge, key));
        } else {
          // TODO: here would be a good place to LOG
        }
      }
    }
  }

  private void registerStage(final Stage stage) {
    for (final Source source : stage.sources()) {
      final String url = source.get("url");
      final String body = source.get("body");
      html.put(url, body);
      audit.append("<tr><td>" + url + "</td><td>" + source.get("title") + "</td><td>" + source.get("audit") + "<br/>");
    }
  }

  public void validate(final BiConsumer<String, String> checker) {
    html.forEach(checker);
  }
}
