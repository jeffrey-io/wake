package io.jeffrey.web.analysis;

import io.jeffrey.web.assemble.InMemoryAssembler;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

/**
 * This is simply a tool to check spelling and other grammar related things
 */
public class LanguageToolMap {
  public static String checkAssemblyAndReport(final InMemoryAssembler assembly, final Language language) {
    final StringBuilder spelling = new StringBuilder();
    spelling.append("<pre>");
    assembly.validate((url, html) -> {
      spelling.append("<h1>" + url + "</h1>\n");
      try {
        final JLanguageTool langTool = new JLanguageTool(language);
        langTool.activateDefaultPatternRules();
        final Document doc = Jsoup.parse(html);
        final Elements elements = doc.select("p");
        elements.forEach((element) -> {
          final String plain = HtmlCleaner.getPlainText(element);
          for (final String badword : BadWords.LIST) {
            if (plain.contains(badword)) {
              spelling.append("<h3>WARNING: contains '" + badword + "'</h3>");
            }
          }
          try {
            final List<RuleMatch> matches = langTool.check(plain);
            if (matches.size() > 0) {
              boolean shown = false;
              for (final RuleMatch match : matches) {
                // TODO: learn how to remove rules
                if (match.getMessage().contains("you repeated a whitespace")) {
                  continue;
                }
                if (!shown) {
                  spelling.append(plain);
                  spelling.append("\n");
                  shown = true;
                }
                spelling.append("Potential error at line " + match.getLine() + ", column " + match.getColumn() + ": " + match.getMessage());
                spelling.append("\n");
                spelling.append("Suggested correction: " + match.getSuggestedReplacements());
                spelling.append("\n");
              }
            }
          } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
          }
        })  ;
      } catch (final Exception err) {
        System.err.println("Exception checking:" + url);
        err.printStackTrace();
      }
    });
    spelling.append("</pre>");
    return spelling.toString();
  }
}
