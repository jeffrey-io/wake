package io.jeffrey.web.analysis;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * This was stolen from:
 * https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java
 * <p>
 * and tweaked to make spelling easier.
 */
public class HtmlCleaner {
  // the formatting rules, implemented in a breadth-first DOM traverse
  private static class FormattingVisitor implements NodeVisitor {
    private final StringBuilder accum = new StringBuilder(); // holds the accumulated text

    // appends text to the string builder with a simple word wrap method
    private void append(final String text) {
      if (text.equals(" ") && (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
        return; // don't accumulate long runs of empty spaces
      }

      accum.append(text);
    }

    // hit when the node is first seen
    @Override
    public void head(final Node node, final int depth) {
      final String name = node.nodeName();
      if (node instanceof TextNode) {
        append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
      } else if (name.equals("li")) {
        append("\n");
      }
    }

    // hit when all of the node's children (if any) have been visited
    @Override
    public void tail(final Node node, final int depth) {
      final String name = node.nodeName();
      if (name.equals("br")) {
        append("\n");
      } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5")) {
        append("\n");
      }
    }

    @Override
    public String toString() {
      return accum.toString();
    }
  }

  /**
   * Format an Element to plain-text
   *
   * @param element the root element to format
   * @return formatted text
   */
  public static String getPlainText(final Element element) {
    final FormattingVisitor formatter = new FormattingVisitor();
    new NodeTraversor(formatter).traverse(element); // walk the DOM, and call .head() and .tail() for each node
    String plain = formatter.toString();
    plain = plain.replaceAll(" \\s*", " ");
    plain = plain.replaceAll("\\s* ", " ");
    plain = plain.replaceAll("\\. ", ". \n");
    return plain;
  }
}
