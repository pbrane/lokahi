package org.opennms.horizon.minion.plugin.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyRegistrationService implements RegistrationService {

    private final Logger logger = LoggerFactory.getLogger(DummyRegistrationService.class);

    @Override
    public void notifyOfPluginRegistration(PluginMetadata pluginMetadata) {
        logger.info("Detected plugin at runtime {}. Skipping its online registration for now.", pluginMetadata);
    }
}
