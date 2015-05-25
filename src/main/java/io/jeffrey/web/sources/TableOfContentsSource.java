/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

/**
 * Find <hX></hX> and inject anchors and build topology
 * TODO: clean up and document
 */
public class TableOfContentsSource extends Source {
  private static class TOCStreamEntry {
    private static String linkHrefName(final String x) {
      final StringBuilder name = new StringBuilder();
      boolean lastWasUnderscore = false;
      for (final char ch : x.toCharArray()) {
        if (Character.isLetterOrDigit(ch)) {
          if (lastWasUnderscore) {
            name.append("_");
          }
          name.append(ch);
          lastWasUnderscore = false;
        } else {
          lastWasUnderscore = true;
        }
      }
      return name.toString();
    }

    public final int    level;
    public final String title;

    public final String name;

    public TOCStreamEntry(final int level, final String html) {
      this.level = level;
      final Document doc = Jsoup.parse(html);
      this.title = new HtmlToPlainText().getPlainText(doc).trim();
      this.name = linkHrefName(title);
    }

    public TOCStreamEntry(final TOCStreamEntry entry, final int adjLevel) {
      this.level = entry.level - adjLevel;
      this.title = entry.title;
      this.name = entry.name;
    }
  }

  private static class TOCTree {
    private final TOCStreamEntry     item;
    private final ArrayList<TOCTree> children;

    public TOCTree() {
      this.item = null;
      this.children = new ArrayList<>();
    }

    public TOCTree(final TOCStreamEntry entry) {
      this.item = entry;
      this.children = new ArrayList<>();
    }

    public ArrayList<HashMap<String, Object>> standardize() {
      final ArrayList<HashMap<String, Object>> result = new ArrayList<>();
      for (final TOCTree subtree : children) {
        final HashMap<String, Object> standard = new HashMap<>();
        standard.put("real", subtree.item != null);
        if (subtree.item != null) {
          standard.put("name", subtree.item.name);
          standard.put("title", subtree.item.title);
          standard.put("level", subtree.item.level);
          final ArrayList<String> push = new ArrayList<>();
          for (int k = 0; k < subtree.item.level; k++) {
            push.add("");
          }
          standard.put("push", push);
        }
        standard.put("children", subtree.standardize());
        result.add(standard);
      }
      return result;
    }
  }

  private static Pattern                     HEADER_TAG = Pattern.compile("<h([1-6])[^>]*>");
  private final Source                       source;

  private ArrayList<HashMap<String, Object>> entries;

  private String                             newBody;

  public TableOfContentsSource(final Source source) {
    this.source = source;
    this.entries = null;
    this.newBody = null;
  }

  private TOCTree assembleStream(final ArrayList<TOCStreamEntry> raw) {
    final ArrayList<TOCStreamEntry> stream = new ArrayList<>();
    int minLevel = 10000;
    for (final TOCStreamEntry entry : raw) {
      minLevel = Math.min(entry.level, minLevel);
    }
    for (final TOCStreamEntry entry : raw) {
      stream.add(new TOCStreamEntry(entry, minLevel - 1));
    }
    final TOCTree root = new TOCTree();
    final Stack<TOCTree> stack = new Stack<>();
    stack.push(root);
    for (final TOCStreamEntry entry : stream) {
      for (int grow = stack.size(); grow < entry.level; grow++) {
        final TOCTree falseParent = new TOCTree();
        stack.peek().children.add(falseParent);
        stack.push(falseParent);
      }
      while (stack.size() > entry.level) {
        stack.pop();
      }
      final TOCTree inject = new TOCTree(entry);
      stack.peek().children.add(inject);
      stack.push(inject);
    }
    return root;
  }

  private String compute(final String body) {
    if (entries != null) {
      return newBody;
    }

    final ArrayList<TOCStreamEntry> stream = new ArrayList<>();
    final StringBuilder next = new StringBuilder();
    int last = 0;

    final String search = body.toLowerCase();
    final Matcher matcher = HEADER_TAG.matcher(search);
    while (matcher.find()) {
      final String closeTag = "</h" + matcher.group(1);
      final int startTagAt = matcher.start();
      final int endTagAt = search.indexOf(closeTag, matcher.end());
      if (endTagAt > 0) {
        final String header = body.substring(matcher.start() + matcher.group().length(), endTagAt);
        final TOCStreamEntry entry = new TOCStreamEntry(Integer.parseInt(matcher.group(1)), header);
        stream.add(entry);
        next.append(body.substring(last, startTagAt));
        next.append("<a name=\"" + entry.name + "\"></a>");
        next.append(matcher.group());
        next.append(header);
        next.append(closeTag);
        last = endTagAt + closeTag.length();
      } else {
        next.append(body.substring(last, startTagAt));
        next.append(matcher.group());
        last = matcher.end();
      }
    }
    next.append(body.substring(last, body.length()));
    this.entries = assembleStream(stream).standardize();
    this.newBody = next.toString();
    return body;
  }

  @Override
  public String get(final String key) {
    if ("body".equals(key)) {
      final String body = source.get("body");
      compute(body);
      return newBody;
    }
    return source.get(key);
  }

  @Override
  public void populateDomain(final Set<String> domain) {
    source.populateDomain(domain);
  }

  @Override
  public void walkComplex(final BiConsumer<String, Object> injectComplex) {
    if (entries == null) {
      compute(source.get("body"));
    }
    source.walkComplex(injectComplex);
    injectComplex.accept("toc", entries);
  }
}
