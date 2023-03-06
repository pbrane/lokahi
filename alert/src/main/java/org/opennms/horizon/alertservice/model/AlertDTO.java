/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alertservice.model;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlertDTO implements Serializable {
    private static final long serialVersionUID = 7275548439687562161L;

    public static final int PROBLEM_TYPE = 1;
    public static final int RESOLUTION_TYPE = 2;
    public static final int PROBLEM_WITHOUT_RESOLUTION_TYPE = 3;

    public static final String ARCHIVED = "Archived";

    private Long alertId;
    private String eventUei;
    private String reductionKey;
    private Integer alertType;
    private Integer ifIndex;
    private Integer counter;
    private AlertSeverity severity = AlertSeverity.INDETERMINATE;
    private Date firstEventTime;
    private Date lastEventTime;
    private Date firstAutomationTime;
    private Date lastAutomationTime;
    private String description;
    private String logMsg;
    private String operInstruct;
    private String mouseOverText;
    private Date suppressedUntil;
    private String suppressedUser;
    private Date suppressedTime;
    private String alertAckUser;
    private Date alertAckTime;
    private Long lastEventId;
    private String clearUei;
    private String clearKey;
    private String managedObjectInstance;
    private String managedObjectType;
    private String applicationDN;
    private String ossPrimaryKey;
    private String x733AlertType;
    private String qosAlertState;
    private int x733ProbableCause = 0;
    private Map<String, String> details;
    private MemoDTO stickyMemo;
    private ReductionKeyMemoDTO reductionKeyMemo;
    private Set<AlertAssociationDTO> associatedAlerts = new HashSet<>();
    private Set<AlertDTO> relatedSituations = new HashSet<>();
    private boolean situation;
    private boolean partOfSituation;
    private AlertSeverity lastEventSeverity;

    /**
     * minimal constructor
     *
     * @param alertid a {@link Integer} object.
     * @param eventuei a {@link String} object.
     * @param counter a {@link Integer} object.
     * @param severity a {@link Integer} object.
     * @param firsteventtime a {@link Date} object.
     */
    public AlertDTO(Long alertid, String eventuei, Integer counter, Integer severity, Date firsteventtime, Date lasteEventTime) {
        this.alertId = alertid;
        this.eventUei = eventuei;
        this.counter = counter;
        this.severity = AlertSeverity.get(severity);
        this.firstEventTime = firsteventtime;
        setLastEventTime(lasteEventTime);
    }

    public String getSeverityLabel() {
        return this.severity.name();
    }

    public void setSeverityLabel(final String label) {
        severity = AlertSeverity.get(label);
    }

    public Integer getSeverityId() {
        return this.severity.getId();
    }

    public void setSeverityId(final Integer severity) {
        this.severity = AlertSeverity.get(severity);
    }

    @Transient
    public boolean isAcknowledged() {
        return getAlertAckUser() != null;
    }

    //TODO: Maybe we DO need the lastEvent?
//    @Transient
//    @XmlElementWrapper(name="parameters")
//    @XmlElement(name="parameter")
//    public List<OnmsEventParameter> getEventParameters() {
//        return m_lastEvent != null ? m_lastEvent.getEventParameters() : null;
//    }

//    public Optional<OnmsEventParameter> findEventParameter(final String name) {
//        return this.getEventParameters().stream().filter(p -> Objects.equals(name, p.getName())).findAny();
//    }
//
//    public String getEventParameter(final String name) {
//        return this.getEventParameters().stream().filter(p -> Objects.equals(name, p.getName())).findAny().map(OnmsEventParameter::getValue).orElse(null);
//    }

    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("alertid", getAlertId())
            .add("uei", getEventUei())
            .add("severity", getSeverity())
            .add("lastEventTime",getLastEventTime())
            .add("counter", getCounter())
            .toString();
    }

    /**
     * This marks an alert as archived and prevents it from being used again in during reduction.
     */
    public void archive() {
        qosAlertState = ARCHIVED;
        severity = AlertSeverity.CLEARED;
        reductionKey = getReductionKey() + ":ID:"+ getAlertId();
    }

    // Alerts that are archived
    @Transient
    public boolean isArchived() {
        return ARCHIVED.equals(qosAlertState);
    }

    /**
     * <p>getRelatedAlerts</p>
     *
     * @return a {@link Set} object.
     */
    public Set<AlertDTO> getRelatedAlerts() {
        return associatedAlerts.stream().map(AlertAssociationDTO::getRelatedAlert).collect(Collectors.toSet());
    }
    
    public Set<Long> getRelatedAlertIds() {
        return getRelatedAlerts().stream()
                .map(AlertDTO::getAlertId)
                .collect(Collectors.toSet());
    }

    public Set<AlertAssociationDTO> getAssociatedAlerts() {
        return associatedAlerts;
    }

    public void setAssociatedAlerts(Set<AlertAssociationDTO> alerts) {
        associatedAlerts = alerts;
        situation = !associatedAlerts.isEmpty();
    }

    public void setRelatedAlerts(Set<AlertDTO> alerts) {
        associatedAlerts.clear();
        alerts.forEach(relatedAlert -> associatedAlerts.add(new AlertAssociationDTO(this, relatedAlert)));
        situation = !associatedAlerts.isEmpty();
    }

    public void setRelatedAlerts(Set<AlertDTO> alerts, Date associationEventTime) {
        associatedAlerts.clear();
        alerts.forEach(relatedAlert -> associatedAlerts.add(new AlertAssociationDTO(this, relatedAlert, associationEventTime)));
        situation = !associatedAlerts.isEmpty();
    }

    public void addRelatedAlert(AlertDTO alert) {
        associatedAlerts.add(new AlertAssociationDTO(this, alert));
        situation = !associatedAlerts.isEmpty();
    }

    public void removeRelatedAlert(AlertDTO alert) {
        associatedAlerts.removeIf(associatedAlert -> associatedAlert.getRelatedAlert().getAlertId().equals(alert.getAlertId()));
        situation = !associatedAlerts.isEmpty();
    }

    public void removeRelatedAlertWithId(Integer relatedAlertId) {
        associatedAlerts.removeIf(associatedAlert -> associatedAlert.getRelatedAlert().getAlertId().equals(relatedAlertId));
        situation = !associatedAlerts.isEmpty();
    }

    public Set<Long> getRelatedSituationIds() {
        return getRelatedSituations().stream()
                .map(AlertDTO::getAlertId)
                .collect(Collectors.toSet());
    }

    public void setRelatedSituations(Set<AlertDTO> alerts) {
        relatedSituations = alerts;
        partOfSituation = !relatedSituations.isEmpty();
    }

    @Transient
    public Date getLastUpdateTime() {
        if (getLastAutomationTime() != null && getLastAutomationTime().compareTo(getLastEventTime()) > 0) {
            return getLastAutomationTime();
        }
        return getLastEventTime();
    }

}
