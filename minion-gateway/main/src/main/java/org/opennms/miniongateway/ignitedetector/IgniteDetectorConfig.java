package org.opennms.miniongateway.ignitedetector;

import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;
import org.opennms.miniongateway.detector.server.LocalDetectorAdapterStubImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteDetectorConfig {
    @Bean("localDetectorAdapter")
    public LocalDetectorAdapter localDetectorAdapter() {
        return new LocalDetectorAdapterStubImpl();
    }


}
