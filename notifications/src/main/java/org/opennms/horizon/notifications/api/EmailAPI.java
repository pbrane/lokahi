package org.opennms.horizon.notifications.api;

import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.notifications.exceptions.NotificationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailAPI {
    private final JavaMailSender sender;

    public void postNotification(Alert alert) throws NotificationException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@opennms.com");
        message.setTo("bdennerley@nanthealth.com");
        message.setSubject("Alarm!");
        message.setText("Do something");
        sender.send(message);
    }
}
