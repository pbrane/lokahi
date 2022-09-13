package org.opennms.miniongateway.ignitedetector;

import javax.annotation.PostConstruct;
import org.apache.ignite.Ignite;
import org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher;
import org.opennms.horizon.shared.ignite.remoteasync.service.IgniteRequestDispatcherFactory;
import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;
import org.opennms.miniongateway.detector.server.LocalDetectorAdapterStubImpl;
import org.springframework.context.annotation.Bean;

public class IgniteDetectorConfig {
    @Bean("localDetectorAdapter")
    public LocalDetectorAdapter localDetectorAdapter() {
        return new LocalDetectorAdapterStubImpl();
    }

    @PostConstruct
    public void requestDispatcher(Ignite ignite) {
        IgniteRequestDispatcherFactory factory = new IgniteRequestDispatcherFactory(ignite);
        factory.init();
    }

}
