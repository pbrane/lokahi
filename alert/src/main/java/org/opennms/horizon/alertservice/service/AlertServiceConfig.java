package org.opennms.horizon.alertservice.service;

import org.opennms.horizon.alertservice.drools.DroolsAlertContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertServiceConfig {

    @Bean("alertMapper")
    public AlertMapper alertMapper() {
        return AlertMapper.INSTANCE;
    }

    @Bean("alertLifecycleListenerManger")
    public AlertLifecycleListenerManager alertLifecycleListenerManager(
        @Autowired DroolsAlertContext droolsAlertContext) {

        AlertLifecycleListenerManager alertLifecycleListenerManager = new AlertLifecycleListenerManager();

        alertLifecycleListenerManager.setListener(droolsAlertContext);

        return alertLifecycleListenerManager;
    }
}
