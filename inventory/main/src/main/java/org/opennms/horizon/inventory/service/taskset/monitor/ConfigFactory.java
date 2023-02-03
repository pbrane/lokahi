package org.opennms.horizon.inventory.service.taskset.monitor;

public interface ConfigFactory {

    ConfigParser<?> createParser(String pluginName);

}
