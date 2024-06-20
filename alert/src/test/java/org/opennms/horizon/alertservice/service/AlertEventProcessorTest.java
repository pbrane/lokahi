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
package org.opennms.horizon.alertservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.alerts.proto.Severity;
import org.opennms.horizon.alertservice.db.entity.AlertCondition;
import org.opennms.horizon.alertservice.db.entity.AlertDefinition;
import org.opennms.horizon.alertservice.db.entity.Location;
import org.opennms.horizon.alertservice.db.entity.MonitorPolicy;
import org.opennms.horizon.alertservice.db.entity.Node;
import org.opennms.horizon.alertservice.db.entity.PolicyRule;
import org.opennms.horizon.alertservice.db.entity.Tag;
import org.opennms.horizon.alertservice.db.repository.AlertDefinitionRepository;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.db.repository.LocationRepository;
import org.opennms.horizon.alertservice.db.repository.NodeRepository;
import org.opennms.horizon.alertservice.db.repository.TagRepository;
import org.opennms.horizon.alertservice.db.tenant.TenantLookup;
import org.opennms.horizon.alertservice.mapper.AlertMapper;
import org.opennms.horizon.alertservice.mapper.AlertMapperImpl;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventParameter;

@ExtendWith(MockitoExtension.class)
class AlertEventProcessorTest {
    private static final String TEST_TENANT_ID = "tenantA";

    @InjectMocks
    AlertEventProcessor processor;

    @Mock
    AlertRepository alertRepository;

    @Mock
    NodeRepository nodeRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AlertDefinitionRepository alertDefinitionRepository;

    @InjectMocks
    AlertUtilServiceImpl alertUtilService;

    @Mock
    AlertReductionKeyExpander alertReductionKeyExpander;

    @Mock // for InjectMocks
    ReductionKeyService reductionKeyService;

    ;

    @Mock
    MeterRegistry registry;

    @Mock
    TagRepository tagRepository;

    @Mock
    TenantLookup tenantLookup;

    @Mock
    Counter counter;

    @Spy // for InjectMocks
    AlertMapper alertMapper = new AlertMapperImpl();

    @BeforeEach
    void setUp() {
        when(registry.counter(any())).thenReturn(counter);
        when(tenantLookup.lookupTenantId(any())).thenReturn(Optional.of(TEST_TENANT_ID));
        processor.init();
        alertReductionKeyExpander = new AlertReductionKeyExpander(alertUtilService);
        reductionKeyService = new ReductionKeyService(alertReductionKeyExpander);
        processor = new AlertEventProcessor(
                alertRepository,
                alertMapper,
                alertDefinitionRepository,
                null,
                reductionKeyService,
                tagRepository,
                registry,
                tenantLookup,
                nodeRepository,
                locationRepository);
    }

    @Test
    void generateAlert() {
        Event event = Event.newBuilder()
                .setTenantId(TEST_TENANT_ID)
                .setUei("uei")
                .setDescription("desc")
                .setNodeId(10L)
                .setLocationId("11")
                .setLocationName("locationName")
                .build();

        AlertCondition alertCondition = getAlertCondicion();

        AlertDefinition alertDefinition = getAlertDefinition(alertCondition);
        MonitorPolicy policy = new MonitorPolicy();
        policy.setName("policyName");
        PolicyRule rule = new PolicyRule();
        rule.setName("ruleName");
        rule.setPolicy(policy);
        alertCondition.setRule(rule);

        MonitorPolicy monitorPolicy = new MonitorPolicy();
        monitorPolicy.setId(1L);
        monitorPolicy.setTenantId(TEST_TENANT_ID);

        var tag = new Tag();
        tag.getPolicies().add(monitorPolicy);

        when(alertDefinitionRepository.findByTenantIdAndUei(event.getTenantId(), event.getUei()))
                .thenReturn(List.of(alertDefinition));

        when(tagRepository.findByTenantIdAndNodeId(anyString(), anyLong())).thenReturn(List.of(tag));

        var node = new Node();
        node.setId(event.getNodeId());
        node.setNodeLabel("nodeLabel");
        when(nodeRepository.findByIdAndTenantId(event.getNodeId(), event.getTenantId()))
                .thenReturn(Optional.of(node));

        List<Alert> alerts = processor.process(event);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0))
                .returns(TEST_TENANT_ID, Alert::getTenantId)
                .returns("desc", Alert::getDescription)
                .returns(alertCondition.getSeverity(), Alert::getSeverity)
                .returns(List.of(monitorPolicy.getId()), Alert::getMonitoringPolicyIdList);

        ArgumentCaptor<Location> saveLocationArg = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository, times(1)).save(saveLocationArg.capture());

        Assertions.assertEquals(saveLocationArg.getValue().getLocationName(), event.getLocationName());
        Assertions.assertEquals(saveLocationArg.getValue().getId(), Long.parseLong(event.getLocationId()));
        Assertions.assertEquals(saveLocationArg.getValue().getTenantId(), event.getTenantId());
    }

    @Test
    public void generateReductionKey() {
        Event event = generateEventBuilder(
                        "uei.opennms.org/vendor/cisco/ciscoIpMRouteMib/traps/ciscoIpMRouteMissingHeartBeats")
                .addParameters(EventParameter.newBuilder()
                        .setName(".1.3.6.1.4.1.2636.1.2.3.4")
                        .setValue("123456")
                        .setType("TimeTicks")
                        .setEncoding("text")
                        .build())
                .addParameters(EventParameter.newBuilder()
                        .setName(".1.3.6.1.2.1.2.2.1.2.1")
                        .setValue("eth0")
                        .setType("OctetString")
                        .setEncoding("text")
                        .build())
                .setIpAddress("127.0.0.1")
                .build();
        AlertCondition alertCondition = getAlertCondicion();
        AlertDefinition alertDefinition = getAlertDefinition(alertCondition);
        alertDefinition.setReductionKey("%uei%:%dpname%:%nodeid%:%interface%:%parm[#1]%:%parm[#2]%");
        MonitorPolicy policy = new MonitorPolicy();
        policy.setName("policyName");
        PolicyRule rule = new PolicyRule();
        rule.setName("ruleName");
        rule.setPolicy(policy);
        alertCondition.setRule(rule);
        MonitorPolicy monitorPolicy = new MonitorPolicy();
        monitorPolicy.setId(1L);
        monitorPolicy.setTenantId(TEST_TENANT_ID);

        var tag = new Tag();
        tag.getPolicies().add(monitorPolicy);

        when(alertDefinitionRepository.findByTenantIdAndUei(event.getTenantId(), event.getUei()))
                .thenReturn(List.of(alertDefinition));

        when(tagRepository.findByTenantIdAndNodeId(anyString(), anyLong())).thenReturn(List.of(tag));

        var node = new Node();
        node.setId(event.getNodeId());
        node.setNodeLabel("nodeLabel");
        when(nodeRepository.findByIdAndTenantId(event.getNodeId(), event.getTenantId()))
                .thenReturn(Optional.of(node));

        List<Alert> alerts = processor.process(event);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0))
                .returns(TEST_TENANT_ID, Alert::getTenantId)
                .returns("desc", Alert::getDescription)
                .returns(alertCondition.getSeverity(), Alert::getSeverity)
                .returns(List.of(monitorPolicy.getId()), Alert::getMonitoringPolicyIdList);
        assertThat(alerts.get(0).getReductionKey())
                .isEqualTo(new StringBuilder(event.getUei())
                        .append(":")
                        .append(event.getLocationId())
                        .append(":")
                        .append(event.getNodeId())
                        .append(":")
                        .append(event.getIpAddress())
                        .append(":")
                        .append(event.getParametersList().get(0).getValue())
                        .append(":")
                        .append(event.getParametersList().get(1).getValue())
                        .toString());

        ArgumentCaptor<Location> saveLocationArg = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository, times(1)).save(saveLocationArg.capture());

        Assertions.assertEquals(saveLocationArg.getValue().getLocationName(), event.getLocationName());
        Assertions.assertEquals(saveLocationArg.getValue().getId(), Long.parseLong(event.getLocationId()));
        Assertions.assertEquals(saveLocationArg.getValue().getTenantId(), event.getTenantId());
    }

    @Test
    void generateMultipleAlertsWithClear() {
        final String reductionKey = "reductionKey";
        final String cleanKey = "cleanKey";
        final String uei = "uei";

        Event event = generateEventBuilder(uei).build();

        AlertCondition alertCondition = getAlertCondicion();

        AlertDefinition alertDefinition = new AlertDefinition();
        alertDefinition.setTenantId(TEST_TENANT_ID);
        alertDefinition.setUei(uei);
        alertDefinition.setReductionKey(reductionKey);
        alertDefinition.setClearKey(cleanKey);
        alertDefinition.setAlertCondition(alertCondition);

        MonitorPolicy policy = new MonitorPolicy();
        policy.setName("policyName");
        PolicyRule rule = new PolicyRule();
        rule.setName("ruleName");
        rule.setPolicy(policy);
        alertCondition.setRule(rule);

        MonitorPolicy monitorPolicy = new MonitorPolicy();
        monitorPolicy.setId(1L);
        monitorPolicy.setTenantId(TEST_TENANT_ID);

        var existingAlert = new org.opennms.horizon.alertservice.db.entity.Alert();
        existingAlert.setClearKey(cleanKey);
        existingAlert.setEventUei(uei);
        existingAlert.setSeverity(Severity.CLEARED);

        var tag = new Tag();
        tag.getPolicies().add(monitorPolicy);

        when(alertDefinitionRepository.findByTenantIdAndUei(event.getTenantId(), event.getUei()))
                .thenReturn(List.of(alertDefinition));

        when(tagRepository.findByTenantIdAndNodeId(anyString(), anyLong())).thenReturn(List.of(tag));

        when(alertRepository.findByReductionKeyAndTenantId(cleanKey, TEST_TENANT_ID))
                .thenReturn(Optional.of(existingAlert));

        var node = new Node();
        node.setId(event.getNodeId());
        node.setNodeLabel("nodeLabel");
        when(nodeRepository.findByIdAndTenantId(event.getNodeId(), event.getTenantId()))
                .thenReturn(Optional.of(node));

        List<Alert> alerts = processor.process(event);

        // verify(reductionKeyService, times(1)).renderArchiveReductionKey(existingAlert, event);
        // verify(reductionKeyService, times(1)).renderArchiveClearKey(existingAlert, event);
        verify(alertRepository, times(1)).saveAndFlush(existingAlert);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0))
                .returns(TEST_TENANT_ID, Alert::getTenantId)
                .returns("desc", Alert::getDescription)
                .returns(alertCondition.getSeverity(), Alert::getSeverity)
                .returns(List.of(monitorPolicy.getId()), Alert::getMonitoringPolicyIdList);

        ArgumentCaptor<Location> saveLocationArg = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository, times(1)).save(saveLocationArg.capture());

        Assertions.assertEquals(saveLocationArg.getValue().getLocationName(), event.getLocationName());
        Assertions.assertEquals(saveLocationArg.getValue().getId(), Long.parseLong(event.getLocationId()));
        Assertions.assertEquals(saveLocationArg.getValue().getTenantId(), event.getTenantId());
    }

    public Event.Builder generateEventBuilder(String uei) {
        return Event.newBuilder()
                .setTenantId(TEST_TENANT_ID)
                .setUei(uei)
                .setDescription("desc")
                .setNodeId(10L)
                .setLocationId("11");
    }

    public AlertCondition getAlertCondicion() {
        AlertCondition alertCondition = new AlertCondition();
        alertCondition.setTenantId(TEST_TENANT_ID);
        alertCondition.setId(1L);
        alertCondition.setSeverity(Severity.MAJOR);
        alertCondition.setCount(1);
        alertCondition.setOvertime(0);
        return alertCondition;
    }

    private AlertDefinition getAlertDefinition(AlertCondition alertCondition) {
        AlertDefinition alertDefinition = new AlertDefinition();
        alertDefinition.setTenantId(TEST_TENANT_ID);
        alertDefinition.setUei("uei");
        alertDefinition.setReductionKey("reduction");
        alertDefinition.setAlertCondition(alertCondition);
        return alertDefinition;
    }
}
