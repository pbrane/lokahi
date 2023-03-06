/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.horizon.shared.dto.event;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AlertDTO {
    private Integer id;
    private String uei;
    private String location;
    private Integer nodeId;
    private String nodeLabel;
    private String ipAddress;
    private ServiceTypeDTO serviceType;
    private String reductionKey;
    private Integer type;
    private Integer count;
    private String severity;
    private Date firstEventTime;
    private String description;
    private String logMessage;
    private String operatorInstructions;
    private String troubleTicket;
    private Integer troubleTicketState;
    private String troubleTicketLink;
    private String mouseOverText;
    private Date suppressedUntil;
    private String suppressedBy;
    private Date suppressedTime;
    private String ackUser;
    private Date ackTime;
    private String clearKey;
    private EventDTO lastEvent;
    private List<EventParameterDTO> parameters;
    private Date lastEventTime;
    private String applicationDN;
    private String managedObjectInstance;
    private String managedObjectType;
    private String ossPrimaryKey;
    private String x733AlertType;
    private Integer x733ProbableCause;
    private String qosAlertState;
    private Date firstAutomationTime;
    private Date lastAutomationTime;
    private Integer ifIndex;
    private ReductionKeyMemoDTO reductionKeyMemo;
    private MemoDTO stickyMemo;
    private List<AlertSummaryDTO> relatedAlerts;
    private Integer affectedNodeCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ServiceTypeDTO getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeDTO serviceType) {
        this.serviceType = serviceType;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getFirstEventTime() {
        return firstEventTime;
    }

    public void setFirstEventTime(Date firstEventTime) {
        this.firstEventTime = firstEventTime;
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

    public String getOperatorInstructions() {
        return operatorInstructions;
    }

    public void setOperatorInstructions(String operatorInstructions) {
        this.operatorInstructions = operatorInstructions;
    }

    public String getTroubleTicket() {
        return troubleTicket;
    }

    public void setTroubleTicket(String troubleTicket) {
        this.troubleTicket = troubleTicket;
    }

    public Integer getTroubleTicketState() {
        return troubleTicketState;
    }

    public void setTroubleTicketState(Integer troubleTicketState) {
        this.troubleTicketState = troubleTicketState;
    }

    public void setTroubleTicketLink(String troubleTicketLink) {
        this.troubleTicketLink = troubleTicketLink;
    }

    public String getTroubleTicketLink() {
        return troubleTicketLink;
    }

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
    }

    public Date getSuppressedUntil() {
        return suppressedUntil;
    }

    public void setSuppressedUntil(Date suppressedUntil) {
        this.suppressedUntil = suppressedUntil;
    }

    public String getSuppressedBy() {
        return suppressedBy;
    }

    public void setSuppressedBy(String suppressedBy) {
        this.suppressedBy = suppressedBy;
    }

    public Date getSuppressedTime() {
        return suppressedTime;
    }

    public void setSuppressedTime(Date suppressedTime) {
        this.suppressedTime = suppressedTime;
    }

    public String getAckUser() {
        return ackUser;
    }

    public void setAckUser(String ackUser) {
        this.ackUser = ackUser;
    }

    public Date getAckTime() {
        return ackTime;
    }

    public void setAckTime(Date ackTime) {
        this.ackTime = ackTime;
    }

    public String getClearKey() {
        return clearKey;
    }

    public void setClearKey(String clearKey) {
        this.clearKey = clearKey;
    }

    public EventDTO getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(EventDTO lastEvent) {
        this.lastEvent = lastEvent;
    }

    public List<EventParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(List<EventParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public Date getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public String getApplicationDN() {
        return applicationDN;
    }

    public void setApplicationDN(String applicationDN) {
        this.applicationDN = applicationDN;
    }

    public String getManagedObjectInstance() {
        return managedObjectInstance;
    }

    public void setManagedObjectInstance(String managedObjectInstance) {
        this.managedObjectInstance = managedObjectInstance;
    }

    public String getManagedObjectType() {
        return managedObjectType;
    }

    public void setManagedObjectType(String managedObjectType) {
        this.managedObjectType = managedObjectType;
    }

    public String getOssPrimaryKey() {
        return ossPrimaryKey;
    }

    public void setOssPrimaryKey(String ossPrimaryKey) {
        this.ossPrimaryKey = ossPrimaryKey;
    }

    public String getX733AlertType() {
        return x733AlertType;
    }

    public void setX733AlertType(String x733AlertType) {
        this.x733AlertType = x733AlertType;
    }

    public Integer getX733ProbableCause() {
        return x733ProbableCause;
    }

    public void setX733ProbableCause(Integer x733ProbableCause) {
        this.x733ProbableCause = x733ProbableCause;
    }

    public String getQosAlertState() {
        return qosAlertState;
    }

    public void setQosAlertState(String qosAlertState) {
        this.qosAlertState = qosAlertState;
    }

    public Date getFirstAutomationTime() {
        return firstAutomationTime;
    }

    public void setFirstAutomationTime(Date firstAutomationTime) {
        this.firstAutomationTime = firstAutomationTime;
    }

    public Date getLastAutomationTime() {
        return lastAutomationTime;
    }

    public void setLastAutomationTime(Date lastAutomationTime) {
        this.lastAutomationTime = lastAutomationTime;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public ReductionKeyMemoDTO getReductionKeyMemo() {
        return reductionKeyMemo;
    }

    public void setReductionKeyMemo(ReductionKeyMemoDTO reductionKeyMemo) {
        this.reductionKeyMemo = reductionKeyMemo;
    }

    public MemoDTO getStickyMemo() {
        return stickyMemo;
    }

    public void setStickyMemo(MemoDTO stickyMemo) {
        this.stickyMemo = stickyMemo;
    }

    public List<AlertSummaryDTO> getRelatedAlerts() {
        return relatedAlerts;
    }

    public void setRelatedAlerts(List<AlertSummaryDTO> relatedAlerts) {
        this.relatedAlerts = relatedAlerts;
    }

    public Integer getAffectedNodeCount() {
        return affectedNodeCount;
    }

    public void setAffectedNodeCount(Integer affectedNodeCount) {
        this.affectedNodeCount = affectedNodeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertDTO alertDTO = (AlertDTO) o;
        return Objects.equals(id, alertDTO.id) &&
                Objects.equals(uei, alertDTO.uei) &&
                Objects.equals(location, alertDTO.location) &&
                Objects.equals(nodeId, alertDTO.nodeId) &&
                Objects.equals(nodeLabel, alertDTO.nodeLabel) &&
                Objects.equals(ipAddress, alertDTO.ipAddress) &&
                Objects.equals(serviceType, alertDTO.serviceType) &&
                Objects.equals(reductionKey, alertDTO.reductionKey) &&
                Objects.equals(type, alertDTO.type) &&
                Objects.equals(count, alertDTO.count) &&
                Objects.equals(severity, alertDTO.severity) &&
                Objects.equals(firstEventTime, alertDTO.firstEventTime) &&
                Objects.equals(description, alertDTO.description) &&
                Objects.equals(logMessage, alertDTO.logMessage) &&
                Objects.equals(operatorInstructions, alertDTO.operatorInstructions) &&
                Objects.equals(troubleTicket, alertDTO.troubleTicket) &&
                Objects.equals(troubleTicketState, alertDTO.troubleTicketState) &&
                Objects.equals(troubleTicketLink, alertDTO.troubleTicketLink) &&
                Objects.equals(mouseOverText, alertDTO.mouseOverText) &&
                Objects.equals(suppressedUntil, alertDTO.suppressedUntil) &&
                Objects.equals(suppressedBy, alertDTO.suppressedBy) &&
                Objects.equals(suppressedTime, alertDTO.suppressedTime) &&
                Objects.equals(ackUser, alertDTO.ackUser) &&
                Objects.equals(ackTime, alertDTO.ackTime) &&
                Objects.equals(clearKey, alertDTO.clearKey) &&
                Objects.equals(lastEvent, alertDTO.lastEvent) &&
                Objects.equals(parameters, alertDTO.parameters) &&
                Objects.equals(lastEventTime, alertDTO.lastEventTime) &&
                Objects.equals(applicationDN, alertDTO.applicationDN) &&
                Objects.equals(managedObjectInstance, alertDTO.managedObjectInstance) &&
                Objects.equals(managedObjectType, alertDTO.managedObjectType) &&
                Objects.equals(ossPrimaryKey, alertDTO.ossPrimaryKey) &&
                Objects.equals(x733AlertType, alertDTO.x733AlertType) &&
                Objects.equals(x733ProbableCause, alertDTO.x733ProbableCause) &&
                Objects.equals(qosAlertState, alertDTO.qosAlertState) &&
                Objects.equals(firstAutomationTime, alertDTO.firstAutomationTime) &&
                Objects.equals(lastAutomationTime, alertDTO.lastAutomationTime) &&
                Objects.equals(ifIndex, alertDTO.ifIndex) &&
                Objects.equals(reductionKeyMemo, alertDTO.reductionKeyMemo) &&
                Objects.equals(stickyMemo, alertDTO.stickyMemo) &&
                Objects.equals(relatedAlerts, alertDTO.relatedAlerts) &&
                Objects.equals(affectedNodeCount, alertDTO.affectedNodeCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uei, location, nodeId, nodeLabel, ipAddress, serviceType, reductionKey, type, count, severity,
                            firstEventTime, description, logMessage, operatorInstructions, troubleTicket, troubleTicketState,
                            troubleTicketLink, mouseOverText, suppressedUntil, suppressedBy, suppressedTime, ackUser, ackTime,
                            clearKey, lastEvent, parameters, lastEventTime, applicationDN, managedObjectInstance, managedObjectType,
                            ossPrimaryKey, x733AlertType, x733ProbableCause, qosAlertState, firstAutomationTime, lastAutomationTime,
                            ifIndex, reductionKeyMemo, stickyMemo, relatedAlerts, affectedNodeCount);
    }
}
