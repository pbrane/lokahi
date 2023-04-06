package org.opennms.horizon.email.api;

import lombok.RequiredArgsConstructor;
import org.opennms.horizon.email.exception.EmailException;
import org.opennms.horizon.email.proto.ContentType;
import org.opennms.horizon.email.proto.EmailAddresses;
import org.opennms.horizon.email.proto.EmailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class SmtpEmailApi implements EmailApi {

    @Value("${spring.mail.from}")
    private String fromAddress;

    private final JavaMailSender sender;

    @Override
    public void sendEmail(EmailAddresses addresses, EmailMessage message) throws EmailException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try {
            helper.setTo(addresses.getToList().toArray(new String[0]));
            helper.setCc(addresses.getCcList().toArray(new String[0]));
            helper.setBcc(addresses.getBccList().toArray(new String[0]));
            helper.setFrom(fromAddress);

            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), message.getContentType() == ContentType.HTML);

            sender.send(helper.getMimeMessage());
        } catch (Exception e) {
            // TODO: This exception may be retryable
            throw new EmailException(e);
        }
    }
}
