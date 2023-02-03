package org.opennms.horizon.inventory.service.taskset.monitor;

import com.google.protobuf.Message;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultConfigFactory implements ConfigFactory {

    private final Map<String, ConfigParser<?>> parsers;

    public DefaultConfigFactory(List<ConfigParser<?>> parsers) {
        this.parsers = parsers.stream().collect(Collectors.toMap(
            ConfigParser::getPlugin,
            parser -> parser
        ));
    }

    @Override
    public ConfigParser<?> createParser(String pluginName) {
        return parsers.get(pluginName);
    }

}
