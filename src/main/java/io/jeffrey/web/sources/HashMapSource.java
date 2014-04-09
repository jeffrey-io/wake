package io.jeffrey.web.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by jeffrey on 4/8/14.
 */
public class HashMapSource extends Source {

    private final HashMap<String, String> data;

    public HashMapSource(Map<String, String> map) {
        this.data = new HashMap<>(map);
    }

    public HashMapSource() {
        this.data = new HashMap<>();
    }

    public HashMapSource put(String key, String value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String get(String key) {
        return data.get(key);
    }

    @Override
    public void itemize(Consumer<String> itemizer) {
        for (String key : data.keySet()) {
            itemizer.accept(key);
        }
    }

    @Override
    public void itemize(BiConsumer<String, Object> inject) {
    }
}
