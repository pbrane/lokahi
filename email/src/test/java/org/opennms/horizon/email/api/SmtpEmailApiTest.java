package org.opennms.horizon.email.api;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.email.exception.EmailException;
import org.opennms.horizon.email.proto.ContentType;
import org.opennms.horizon.email.proto.EmailAddresses;
import org.opennms.horizon.email.proto.EmailMessage;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class SmtpEmailApiTest {

    private static final String FROM_ADDRESS = "noreply@test";

    @InjectMocks
    SmtpEmailApi emailApi;

    @Mock
    JavaMailSender sender;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(emailApi, "fromAddress", FROM_ADDRESS);
        Mockito.when(sender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    public void canSendEmailToSingleRecipient() throws EmailException {
        EmailAddresses addresses = EmailAddresses.newBuilder().addTo("frankenstein@monster.com").build();
        EmailMessage message = EmailMessage.newBuilder().setSubject("Gah!").setBody("A monster!").build();
        emailApi.sendEmail(addresses, message);

        MessageMatcher matcher = MessageMatcher.builder()
            .expectedToAddresses(List.of("frankenstein@monster.com"))
            .expectedSubject("Gah!")
            .expectedBody("A monster!")
            .build();
        Mockito.verify(sender, times(1)).send(argThat(matcher));
    }

    @Test
    public void canSendEmailToMultipleRecipients() throws EmailException {
        EmailAddresses addresses = EmailAddresses.newBuilder()
            .addTo("boss@company.com")
            .addTo("ceo@company.com")
            .addCc("developer@company.com")
            .addCc("support@company.com")
            .build();
        EmailMessage message = EmailMessage.newBuilder().setSubject("Alert").build();
        emailApi.sendEmail(addresses, message);

        MessageMatcher matcher = MessageMatcher.builder()
            .expectedToAddresses(List.of("boss@company.com", "ceo@company.com"))
            .expectedCcAddresses(List.of("developer@company.com", "support@company.com"))
            .expectedSubject("Alert")
            .build();
        Mockito.verify(sender, times(1)).send(argThat(matcher));
    }

    @Test
    public void canSendPlaintextEmail() throws EmailException {
        EmailAddresses addresses = EmailAddresses.newBuilder().build();

        // If not specified, default to a plain text message
        EmailMessage message = EmailMessage.newBuilder().build();
        emailApi.sendEmail(addresses, message);
        Mockito.verify(sender, times(1))
            .send(argThat(MessageMatcher.builder().expectedType(MediaType.TEXT_PLAIN).build()));

        // Explicitly plain text
        message = EmailMessage.newBuilder().setContentType(ContentType.PLAIN_TEXT).build();
        emailApi.sendEmail(addresses, message);
        Mockito.verify(sender, times(2))
            .send(argThat(MessageMatcher.builder().expectedType(MediaType.TEXT_PLAIN).build()));
    }

    @Test
    public void throwsOnFailure() {
        Mockito.doThrow(new MailSendException("Connection failure")).when(sender).send(any(MimeMessage.class));
        assertThrows(EmailException.class, () -> emailApi.sendEmail(EmailAddresses.newBuilder().build(), EmailMessage.newBuilder().build()));
    }

    @Builder
    private static class MessageMatcher implements ArgumentMatcher<MimeMessage> {
        private List<String> expectedToAddresses;
        private List<String> expectedCcAddresses;
        private List<String> expectedBccAddresses;
        private String expectedSubject;
        private String expectedBody;

        private MediaType expectedType;

        private List<String> convert(Address[] addresses) {
            if (addresses == null) {
                return null;
            }
            return Arrays.stream(addresses)
                .filter(InternetAddress.class::isInstance)
                .map(InternetAddress.class::cast)
                .map(InternetAddress::getAddress)
                .toList();
        }

        @Override
        public boolean matches(MimeMessage mimeMessage) {
            try {
                assertEquals(expectedToAddresses, convert(mimeMessage.getRecipients(Message.RecipientType.TO)));
                assertEquals(expectedCcAddresses, convert(mimeMessage.getRecipients(Message.RecipientType.CC)));
                assertEquals(expectedBccAddresses, convert(mimeMessage.getRecipients(Message.RecipientType.BCC)));
                assertEquals(List.of(FROM_ADDRESS), convert(mimeMessage.getFrom()));
                if (expectedType != null) {
                    assertEquals(expectedType.toString(), mimeMessage.getContentType());
                }
                if (expectedSubject != null) {
                    assertEquals(expectedSubject, mimeMessage.getSubject());
                }
                if (expectedBody != null) {
                    assertEquals(expectedBody, mimeMessage.getContent());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return true;
        }
    }
}
