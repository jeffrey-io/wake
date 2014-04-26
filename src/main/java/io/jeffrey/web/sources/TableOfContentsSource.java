/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.sources;

import com.amazonaws.services.elasticbeanstalk.model.TooManyConfigurationTemplatesException;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Find <hX></hX> and inject anchors and build topology
 * TODO: clean up and document
 */
public class TableOfContentsSource extends Source {
   private static Pattern HEADER_TAG = Pattern.compile("<h([1-6])[^>]*>");
   private final Source source;
   private ArrayList<HashMap<String, Object>> entries;
   private String newBody;

   public TableOfContentsSource(final Source source) {
      this.source = source;
      this.entries = null;
      this.newBody = null;
   }

   private static class TOCStreamEntry {
      public final int level;
      public final String title;
      public final String name;

      public TOCStreamEntry(TOCStreamEntry entry, int adjLevel) {
         this.level = entry.level - adjLevel;
         this.title = entry.title;
         this.name = entry.name;
      }

      public TOCStreamEntry(int level, String html) {
         this.level = level;
         Document doc = Jsoup.parse(html);
         this.title = new HtmlToPlainText().getPlainText(doc).trim();
         this.name = linkHrefName(title);
      }

      private static String linkHrefName(String x) {
         StringBuilder name = new StringBuilder();
         boolean lastWasUnderscore = false;
         for (char ch : x.toCharArray()) {
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
   }

   private static class TOCTree {
      private TOCStreamEntry item;
      private ArrayList<TOCTree> children;

      public TOCTree() {
         this.item = null;
         this.children = new ArrayList<>();
      }

      public TOCTree(TOCStreamEntry entry) {
         this.item = entry;
         this.children = new ArrayList<>();
      }

      public ArrayList<HashMap<String, Object>> standardize() {
         ArrayList<HashMap<String, Object>> result = new ArrayList<>();
         for (TOCTree subtree : children) {
            HashMap<String, Object> standard = new HashMap<>();
            standard.put("real", subtree.item != null);
            if (subtree.item != null) {
               standard.put("name", subtree.item.name);
               standard.put("title", subtree.item.title);
               standard.put("level", subtree.item.level);
               ArrayList<String> push = new ArrayList<>();
               for (int k = 0; k < subtree.item.level; k++) push.add("");
               standard.put("push", push);
            }
            standard.put("children", subtree.standardize());
            result.add(standard);
         }
         return result;
      }
   }

   private TOCTree assembleStream(ArrayList<TOCStreamEntry> raw) {
      ArrayList<TOCStreamEntry> stream = new ArrayList<>();
      int minLevel = 10000;
      for (TOCStreamEntry entry : raw) {
         minLevel = Math.min(entry.level, minLevel);
      }
      for (TOCStreamEntry entry : raw) {
         stream.add(new TOCStreamEntry(entry, minLevel - 1));
      }
      TOCTree root = new TOCTree();
      Stack<TOCTree> stack = new Stack<>();
      stack.push(root);
      for (TOCStreamEntry entry : stream) {
         for (int grow = stack.size(); grow < entry.level; grow++) {
            TOCTree falseParent = new TOCTree();
            stack.peek().children.add(falseParent);
            stack.push(falseParent);
         }
         while (stack.size() > entry.level) {
            stack.pop();
         }
         TOCTree inject = new TOCTree(entry);
         stack.peek().children.add(inject);
         stack.push(inject);
      }
      return root;
   }

   private String compute(String body) {
      if (entries != null) {
         return newBody;
      }

      ArrayList<TOCStreamEntry> stream = new ArrayList<>();
      StringBuilder next = new StringBuilder();
      int last = 0;

      String search = body.toLowerCase();
      Matcher matcher = HEADER_TAG.matcher(search);
      while (matcher.find()) {
         String closeTag = "</h" + matcher.group(1);
         int startTagAt = matcher.start();
         int endTagAt = search.indexOf(closeTag, matcher.end());
         if (endTagAt > 0) {
            String header = body.substring(matcher.start() + matcher.group().length(), endTagAt);
            TOCStreamEntry entry = new TOCStreamEntry(Integer.parseInt(matcher.group(1)), header);
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
   public String get(String key) {
      if ("body".equals(key)) {
         String body = source.get("body");
         compute(body);
         return newBody;
      }
      return source.get(key);
   }

   @Override
   public void populateDomain(Set<String> domain) {
      source.populateDomain(domain);
   }

   @Override
   public void walkComplex(BiConsumer<String, Object> injectComplex) {
      if (entries == null) {
         compute(source.get("body"));
      }
      source.walkComplex(injectComplex);
      injectComplex.accept("toc", entries);
   }
}
