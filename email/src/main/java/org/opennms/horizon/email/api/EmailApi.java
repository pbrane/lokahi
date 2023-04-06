package org.opennms.horizon.email.api;

import org.opennms.horizon.email.exception.EmailException;
import org.opennms.horizon.email.proto.EmailAddresses;
import org.opennms.horizon.email.proto.EmailMessage;

public interface EmailApi {
    void sendEmail(EmailAddresses addresses, EmailMessage message) throws EmailException;
}
