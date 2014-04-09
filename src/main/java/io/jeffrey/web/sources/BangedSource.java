package io.jeffrey.web.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class BangedSource extends Source {
  private static final String DOT = Pattern.quote(".");
  private final HashMap<String, String> values;
  private int currentLineNumber;

  public BangedSource(File file) throws IOException {
    String name = file.getName().split(DOT)[0];
    this.values = new HashMap<>();
    values.put("name", name);
    values.put("url", name + ".html");
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      final StringBuilder body = new StringBuilder();
      currentLineNumber = 0;
      reader.lines().forEach((ln) -> {
        currentLineNumber++;
        if (ln.startsWith("#!")) {
          indexBang(ln.substring(2).trim());
        } else {
          body.append(ln);
          body.append("\n");
        }
      });
      values.put("body", body.toString());
    } finally {
      reader.close();
    }
  }

  private void indexBang(String unparsedAssignment) {
    int kEq = unparsedAssignment.indexOf('=');
    if (kEq < 0)
      throw new SourceException("there should be an '=' in '" + unparsedAssignment + "' on line " + currentLineNumber);
    String key = unparsedAssignment.substring(0, kEq).trim().toLowerCase();
    if (key.length() == 0)
      throw new SourceException("there should be at least one character before the '=' in '" + unparsedAssignment + "' on line " + currentLineNumber);
    String val = unparsedAssignment.substring(kEq + 1).trim();
    values.put(key, val);
  }

  @Override
  public String get(String key) {
    return values.get(key);
  }

  @Override
  public void itemize(Consumer<String> itemizer) {
    for (String key : values.keySet()) itemizer.accept(key);
  }

  @Override
  public void itemize(BiConsumer<String, Object> inject) {
  }
}
