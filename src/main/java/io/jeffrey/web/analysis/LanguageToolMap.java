package io.jeffrey.web.analysis;

import io.jeffrey.web.assemble.InMemoryAssembler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

/**
 * This is simply a tool to check spelling and other grammar related things
 */
public class LanguageToolMap {
   public static String checkAssemblyAndReport(InMemoryAssembler assembly, Language language) {
      final StringBuilder spelling = new StringBuilder();
      spelling.append("<pre>");
      assembly.validate((url, html) ->
            {
               try {
                  spelling.append("<h1>" + url + "</h1>\n");
                  JLanguageTool langTool = new JLanguageTool(language);
                  langTool.activateDefaultPatternRules();
                  Document doc = Jsoup.parse(html);
                  Elements elements = doc.select("p");
                  elements.forEach((element) -> {
                     String plain = HtmlCleaner.getPlainText(element);
                     plain = plain.replaceAll(" \\s*", " ");
                     plain = plain.replaceAll("\\s* ", " ");
                     try {
                        List<RuleMatch> matches = langTool.check(plain);
                        if (matches.size() > 0) {
                           spelling.append(plain);
                           spelling.append("\n");
                           for (RuleMatch match : matches) {
                              spelling.append("Potential error at line " + match.getLine() + ", column " + match.getColumn() + ": " + match.getMessage());
                              spelling.append("\n");
                              spelling.append("Suggested correction: " + match.getSuggestedReplacements());
                              spelling.append("\n");
                           }
                        }
                     } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                     }
                  });
               } catch (Exception err) {
                  System.err.println("Exception checking:" + url);
                  err.printStackTrace();
               }
            }
      );
      spelling.append("</pre>");
      return spelling.toString();
   }
}