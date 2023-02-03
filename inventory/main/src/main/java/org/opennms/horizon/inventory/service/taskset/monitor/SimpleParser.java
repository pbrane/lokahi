package org.opennms.horizon.inventory.service.taskset.monitor;

import com.google.protobuf.Message;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class SimpleParser<T extends Message> implements ConfigParser<T> {

    private String plugin;

    SimpleParser(String plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPlugin() {
        return plugin;
    }

    protected final void map(Map<String, String> map, String key, String fallback, Consumer<String> consumer) {
        map(map, key, fallback, Function.identity(), consumer);
    }

    protected final void map(Map<String, String> map, String key, boolean fallback, Consumer<Boolean> consumer) {
        map(map, key, () -> fallback, Boolean::parseBoolean, consumer);
    }

    protected final void map(Map<String, String> map, String key, int fallback, Consumer<Integer> consumer) {
        map(map, key, () -> fallback, Integer::parseInt, consumer);
    }

    protected final void map(Map<String, String> map, String key, Long fallback, Consumer<Long> consumer) {
        map(map, key, () -> fallback, Long::parseLong, consumer);
    }

    protected final <X> void map(Map<String, String> map, String key, String fallback, Function<String, X> mapper, Consumer<X> consumer) {
        map(map, key, () -> mapper.apply(fallback), mapper, consumer);
    }

    protected final <X> void map(Map<String, String> map, String key, Supplier<X> fallback, Function<String, X> mapper, Consumer<X> consumer) {
        if (map.containsKey(key)) {
            String value = map.get(key);
            X mappedValue = mapper.apply(value);
            consumer.accept(mappedValue);
            return;
        }

        consumer.accept(fallback.get());
    }
}
