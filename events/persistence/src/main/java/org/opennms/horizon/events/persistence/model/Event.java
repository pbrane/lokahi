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
package org.opennms.horizon.events.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.net.InetAddress;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.opennms.horizon.events.proto.Severity;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_label")
    private String eventLabel;

    @NotNull
    @Column(name = "tenant_id")
    private String tenantId;

    @NotNull
    @Column(name = "event_uei")
    private String eventUei;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.NORMAL;

    @NotNull
    @Column(name = "produced_time", columnDefinition = "TIMESTAMP")
    private LocalDateTime producedTime;

    @Column(name = "monitoring_location_id")
    private Long monitoringLocationId;

    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @Column(name = "event_parameters", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private EventParameters eventParameters;

    @Column(name = "event_info", columnDefinition = "bytea")
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] eventInfo;

    @Column(name = "location_name")
    private String locationName;

    @Column
    private String description;

    @Column(name = "log_message")
    private String logMessage;

    public EventParameters getEventParameters() {
        return eventParameters;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getEventUei() {
        return eventUei;
    }

    public void setEventUei(String eventUei) {
        this.eventUei = eventUei;
    }

    public LocalDateTime getProducedTime() {
        return producedTime;
    }

    public void setProducedTime(LocalDateTime producedTime) {
        this.producedTime = producedTime;
    }

    public Long getMonitoringLocationId() {
        return monitoringLocationId;
    }

    public void setMonitoringLocationId(Long monitoringLocationId) {
        this.monitoringLocationId = monitoringLocationId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setEventParameters(EventParameters eventParameters) {
        this.eventParameters = eventParameters;
    }

    public byte[] getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(byte[] eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public void setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
