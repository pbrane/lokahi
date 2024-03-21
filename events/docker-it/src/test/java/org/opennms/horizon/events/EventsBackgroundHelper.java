/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.protobuf.Empty;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventServiceGrpc;
import org.opennms.horizon.events.proto.EventsSearchBy;
import org.opennms.horizon.events.traps.TrapProducer;
import org.opennms.horizon.grpc.traps.contract.TenantLocationSpecificTrapLogDTO;
import org.opennms.horizon.grpc.traps.contract.TrapLogDTO;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.opennms.horizon.shared.grpc.traps.contract.mapper.TenantLocationSpecificTrapLogDTOMapper;
import org.opennms.horizon.shared.grpc.traps.contract.mapper.impl.TenantLocationSpecificTrapLogDTOMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Getter
public class EventsBackgroundHelper {
    private static final Logger LOG = LoggerFactory.getLogger(EventsBackgroundHelper.class);
    private static final int DEADLINE_DURATION = 60;
    private static final String LOCALHOST = "localhost";
    private String tenantId;
    private EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub;
    private TrapProducer trapProducer;

    private Integer externalGrpcPort;
    private final Map<String, String> grpcHeaders = new TreeMap<>();

    public void externalGRPCPortInSystemProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        externalGrpcPort = Integer.parseInt(value);
        LOG.info("Using External gRPC port {}", externalGrpcPort);
    }

    public void createGrpcConnectionForEvents() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(LOCALHOST, externalGrpcPort);

        ManagedChannel managedChannel = channelBuilder.usePlaintext().build();
        managedChannel.getState(true);
        eventServiceBlockingStub = EventServiceGrpc.newBlockingStub(managedChannel)
                .withInterceptors(prepareGrpcHeaderInterceptor())
                .withDeadlineAfter(DEADLINE_DURATION + 10000, TimeUnit.SECONDS);
    }

    private ClientInterceptor prepareGrpcHeaderInterceptor() {
        return MetadataUtils.newAttachHeadersInterceptor(prepareGrpcHeaders());
    }

    private Metadata prepareGrpcHeaders() {
        Metadata result = new Metadata();
        result.put(GrpcConstants.AUTHORIZATION_BYPASS_KEY, String.valueOf(true));
        result.put(GrpcConstants.TENANT_ID_BYPASS_KEY, tenantId);
        return result;
    }

    public void grpcTenantId(String tenantId) {
        Objects.requireNonNull(tenantId);
        this.tenantId = tenantId;
        grpcHeaders.put(GrpcConstants.TENANT_ID_KEY, tenantId);
        LOG.info("Using tenantId={}", tenantId);
    }

    public int getEventCount() {
        return eventServiceBlockingStub.listEvents(Empty.newBuilder().build()).getEventsCount();
    }

    public void initializeTrapProducer(String topic) {
        this.trapProducer = new TrapProducer();

        // Kafka producer configuration properties
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        // Create a Kafka producer factory
        DefaultKafkaProducerFactory<String, byte[]> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        // Create a Kafka template using the producer factory
        KafkaTemplate<String, byte[]> kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Set the Kafka template and topic in the trap producer
        this.trapProducer.setKafkaTemplate(kafkaTemplate);
        this.trapProducer.setTopic(topic);
    }

    public void sendTrapDataToKafkaListenerViaProducerWithTenantIdAndLocationId(String tenantId, String locationId) {
        TenantLocationSpecificTrapLogDTOMapper tenantLocationSpecificTrapLogDTOMapper =
                new TenantLocationSpecificTrapLogDTOMapperImpl();
        TenantLocationSpecificTrapLogDTO tenantLocationSpecificTrapLogDTO =
                tenantLocationSpecificTrapLogDTOMapper.mapBareToTenanted(
                        tenantId, locationId, TrapLogDTO.newBuilder().build());
        this.trapProducer.sendTrap(tenantLocationSpecificTrapLogDTO);
    }

    public void searchEventWithLocation(int eventsCount, int nodeId, String location) {
        EventsSearchBy searchEventByLocationName = EventsSearchBy.newBuilder()
                .setNodeId(nodeId)
                .setSearchTerm(location)
                .build();
        List<Event> searchEvents =
                eventServiceBlockingStub.searchEvents(searchEventByLocationName).getEventsList();

        assertNotNull(searchEvents);
        assertEquals(eventsCount, searchEvents.size());
    }
}
