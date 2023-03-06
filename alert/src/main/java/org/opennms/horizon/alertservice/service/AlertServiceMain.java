package org.opennms.horizon.alertservice.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@SpringBootApplication
@ComponentScan(basePackages = "org.opennms.horizon.alertservice")
@EnableJpaRepositories(basePackages = "org.opennms.horizon.alertservice.db.repository")
@EntityScan(basePackages = "org.opennms.horizon.alertservice.db.entity")
public class AlertServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(AlertServiceMain.class, args);
    }
}
