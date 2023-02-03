package org.opennms.horizon.inventory.service.taskset.monitor;

import com.google.protobuf.Message;
import java.util.Map;

public interface ConfigParser<T extends Message> {

    T parse(Map<String, String> input);

    String getPlugin();

}
