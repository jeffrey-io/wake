package io.jeffrey.web.stages;

public class CompressHTMLStage extends BodyFinalizerStage {

  private static String trimLines(final String html) {
    final StringBuilder out = new StringBuilder();
    final String[] lines = html.split("\n");
    for (final String line : lines) {
      out.append(line.trim() + "\n");
    }
    return out.toString();
  }

  public CompressHTMLStage(final Stage prior) {
    super(prior, (html) -> {
      String cleaned = trimLines(html);
      // white space around tags
        cleaned = cleaned.replaceAll("\\s+<", " <");
        cleaned = cleaned.replaceAll(">\\s+", "> ");
        return cleaned;
      });
  }
}
