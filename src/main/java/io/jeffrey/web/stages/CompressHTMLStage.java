package io.jeffrey.web.stages;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class CompressHTMLStage extends BodyFinalizerStage {
  public CompressHTMLStage(Stage prior) {
    super(prior, (html) -> {
      return html;
      // return Jsoup.clean(html, Whitelist.relaxed());
    });
  }
}
