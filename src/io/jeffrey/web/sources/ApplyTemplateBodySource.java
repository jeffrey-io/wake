package io.jeffrey.web.sources;

import com.github.jknack.handlebars.Handlebars;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by jeffrey on 3/18/2014.
 */
public class ApplyTemplateBodySource extends Source {
  private static Handlebars compiler = new Handlebars();
  private final Source data;
  private final Source template;

  public ApplyTemplateBodySource(Source data, Source template) {
    this.data = data;
    this.template = template;
  }

  @Override
  public String get(String key) {
    if ("body".equals(key)) {
      try {
        HashMap<String, Object> map = new HashMap<>();
        itemize((item) -> {
          map.put(item, data.get(item));
        });
        itemize((k,v) -> {
          map.put(k,v);
        });
        String ret = template.get("body");
        long hash = ret.hashCode();
        long last = -1;
        while(hash != last) {
          last = hash;
          ret = compiler.compileInline(ret).apply(map);
          hash = ret.hashCode();
        }
        return ret;
      } catch (IOException failed) {
        throw new RuntimeException(failed);
      }
    }
    return data.get(key);
  }

  @Override
  public void itemize(Consumer<String> itemizer) {
    template.itemize(itemizer);
    data.itemize(itemizer);
  }

  @Override
  public void itemize(BiConsumer<String, Object> inject) {
    template.itemize(inject);
    data.itemize(inject);
  }
}
