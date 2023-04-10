/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
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
