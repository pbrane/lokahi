package org.opennms.horizon.email;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class SpringContextTestInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final Integer smtpPort = 1025;
    private static final Integer webPort = 8025;
    public static final GenericContainer<?> mailhog = new GenericContainer<>(DockerImageName.parse("mailhog/mailhog:v1.0.1"))
        .withExposedPorts(webPort, smtpPort);

    static {
        mailhog.start();
    }

    @Override
    public void initialize(@NotNull GenericApplicationContext applicationContext) {
        TestPropertyValues.of(
            "spring.mail.host=" + mailhog.getHost(),
            "spring.mail.port=" + getSmtpPort()
        ).applyTo(applicationContext);
    }

    public static Integer getSmtpPort() {
        return mailhog.getMappedPort(smtpPort);
    }

    public static Integer getWebPort() {
        return mailhog.getMappedPort(webPort);
    }
}
