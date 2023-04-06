package org.opennms.horizon.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.email.api.EmailApi;
import org.opennms.horizon.email.exception.EmailException;
import org.opennms.horizon.email.proto.Email;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final EmailApi emailApi;

    public void sendEmail(Email email) throws EmailException {
        // TODO: If a tenant was specified in the email, lookup the email addresses of users in that tenant via KeyCloak
        emailApi.sendEmail(email.getAddresses(), email.getMessage());
    }
}
