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

package org.opennms.horizon.flows;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.opennms.dataplatform.flows.document.FlowDocument;
import org.opennms.dataplatform.flows.document.FlowDocumentLog;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@PropertySource("classpath:application.yml")
public class FlowProcessor {
    private final FlowIngesterClient ingesterClient;

    public FlowProcessor(final FlowIngesterClient ingesterClient) {
        this.ingesterClient = Objects.requireNonNull(ingesterClient);
    }

    @KafkaListener(topics = "flows", concurrency = "1")
    public void consume(@Payload final byte[] data, @Headers final Map<String, Object> headers) {
        final String tenantId = getTenantId(headers);

        try {
            final FlowDocumentLog flows = FlowDocumentLog.parseFrom(data);

            log.info("Processing flows for {} from location={}", tenantId, flows.getLocation());

            flows.getMessageList().forEach(flow -> CompletableFuture.supplyAsync(() -> {
                try {
                    this.ingesterClient.pushFlowToIngester(FlowDocument.newBuilder(flow)
                        .setTenantId(tenantId)
                        .build());


                } catch (final Exception exc) {
                    log.warn("Error processing flow", exc);
                }
                return null;
            }));
        } catch (InvalidProtocolBufferException e) {
            log.error("Invalid data from kafka", e);
        }
    }

    private String getTenantId(Map<String, Object> headers) {
        return Optional.ofNullable(headers.get(GrpcConstants.TENANT_ID_KEY))
            .map(tenantId -> {
                if (tenantId instanceof byte[]) {
                    return new String((byte[]) tenantId);
                }
                if (tenantId instanceof String) {
                    return (String) tenantId;
                }
                return "" + tenantId;
            })
            .orElseThrow(() -> new RuntimeException("Could not determine tenant id"));
    }
}
