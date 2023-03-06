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

package org.opennms.horizon.alertservice.db.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;
import org.opennms.horizon.alertservice.model.AlertSeverity;

import com.google.common.base.MoreObjects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;

@Entity
@Table(name="alert")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Alert extends TenantAwareEntity implements Serializable {
    private static final long serialVersionUID = 7275548439687562161L;

    public static final int PROBLEM_TYPE = 1;
    public static final int RESOLUTION_TYPE = 2;
    public static final int PROBLEM_WITHOUT_RESOLUTION_TYPE = 3;

    public static final String ARCHIVED = "Archived";


    @Id
    @SequenceGenerator(name="alertSequence", sequenceName="alertNxtId", allocationSize = 1)
    @GeneratedValue(generator="alertSequence")
    @Column(nullable=false)
    private Long alertId;

    @Column(length=256, nullable=false)
    private String eventUei;

    @Column(unique=true)
    private String reductionKey;

    @Column
    private Integer alertType;

    @Column
    private Integer ifIndex;

    @Column(nullable=false)
    private Integer counter;

    @Column(nullable=false)
    @Enumerated(EnumType.ORDINAL)
    private AlertSeverity severity = AlertSeverity.INDETERMINATE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date firstEventTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date lastEventTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date firstAutomationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date lastAutomationTime;

    @Column(length=4000)
    private String description;

    @Column(length=1024)
    private String logMsg;

    @Column
    private String operInstruct;

    @Column(length=64)
    private String mouseOverText;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date suppressedUntil;

    @Column(length=256)
    private String suppressedUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date suppressedTime;

    @Column(length=256)
    private String alertAckUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date alertAckTime;

    @Column
    private Long lastEventId;

    @Column
    private String clearUei;

    @Column
    private String clearKey;

    @Column(length=512)
    private String managedObjectInstance;

    @Column(length=512)
    private String managedObjectType;

    @Column(length=512)
    private String applicationDn;

    @Column(length=512)
    private String ossPrimaryKey;

    @Column(name="x733_alert_type", length=31)
    private String x733AlertType;

    @Column(length=31)
    private String qosAlertState;

    @Column(name="x733_probable_cause", nullable=false)
    private int x733ProbableCause = 0;

    @Column
    private AlertSeverity lastEventSeverity;

    public AlertSeverity getLastEventSeverity() {
        return lastEventSeverity;
    }

    //========== fields with cross table relationships =========

    @ElementCollection
    @JoinTable(name="alert_attributes", joinColumns = @JoinColumn(name="alert_id"))
    @MapKeyColumn(name="attribute_name")
    @Column(name="attribute_value", nullable=false)
    private Map<String, String> details;

    @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="sticky_memo_id")
    @Exclude
    private Memo stickyMemo;

    @OneToMany(mappedBy = "situationAlertId", orphanRemoval = true, cascade = CascadeType.ALL)
    @Exclude
    private Set<AlertAssociation> associatedAlerts = new HashSet<>();

    // a situation is an alert, but an alert is not necessarily a situation
    // a situation may contain many alerts
    // a situation has a set of alerts
    // if an alert is part of a situation, related situation will be non-empty
    @ElementCollection
    @JoinTable(name = "alert_association", joinColumns = @JoinColumn(name = "related_alert_id"),
        inverseJoinColumns = @JoinColumn(name = "situation_alert_id"))
    @Column(name="alert_id", nullable=false)
    private Set<Alert> relatedSituations = new HashSet<>();

    @Formula(value = "(SELECT COUNT(*)>0 FROM ALERT_ASSOCIATION S WHERE S.SITUATION_ALERT_ID=ALERT_ID)")
    private boolean situation;

    @Formula(value = "(SELECT COUNT(*)>0 FROM ALERT_ASSOCIATION S WHERE S.RELATED_ALERT_ID=ALERT_ID)")
    private boolean partOfSituation;

    /**
     * minimal constructor
     *
     * @param alertid a {@link Integer} object.
     * @param eventuei a {@link String} object.
     * @param counter a {@link Integer} object.
     * @param severity a {@link Integer} object.
     * @param firsteventtime a {@link Date} object.
     */
    public Alert(Long alertid, String eventuei, Integer counter, Integer severity, Date firsteventtime, Date lasteEventTime) {
        this.alertId = alertid;
        this.eventUei = eventuei;
        this.counter = counter;
        this.severity = AlertSeverity.get(severity);
        this.firstEventTime = firsteventtime;
        setLastEventTime(lasteEventTime);
    }

    @Transient
    public void incrementCount() {
        counter++;
    }

    @Transient
    public String getSeverityLabel() {
        return this.severity.name();
    }

    public void setSeverityLabel(final String label) {
        severity = AlertSeverity.get(label);
    }

    @Transient
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
    @Transient
    
    public Set<Alert> getRelatedAlerts() {
        return associatedAlerts.stream().map(AlertAssociation::getRelatedAlertId).collect(Collectors.toSet());
    }

    @Transient
    
    public Set<Long> getRelatedAlertIds() {
        return getRelatedAlerts().stream()
                .map(Alert::getAlertId)
                .collect(Collectors.toSet());
    }

    
    @OneToMany(mappedBy = "situationAlertId", orphanRemoval = true, cascade = CascadeType.ALL)
    public Set<AlertAssociation> getAssociatedAlerts() {
        return associatedAlerts;
    }

    public void setAssociatedAlerts(Set<AlertAssociation> alerts) {
        associatedAlerts = alerts;
        situation = !associatedAlerts.isEmpty();
    }

    public void setRelatedAlerts(Set<Alert> alerts) {
        associatedAlerts.clear();
        alerts.forEach(relatedAlert -> associatedAlerts.add(new AlertAssociation(this, relatedAlert)));
        situation = !associatedAlerts.isEmpty();
    }

    public void setRelatedAlerts(Set<Alert> alerts, Date associationEventTime) {
        associatedAlerts.clear();
        alerts.forEach(relatedAlert -> associatedAlerts.add(new AlertAssociation(this, relatedAlert, associationEventTime)));
        situation = !associatedAlerts.isEmpty();
    }

    public void addRelatedAlert(Alert alert) {
        associatedAlerts.add(new AlertAssociation(this, alert));
        situation = !associatedAlerts.isEmpty();
    }

    public void removeRelatedAlert(Alert alert) {
        associatedAlerts.removeIf(associatedAlert -> associatedAlert.getRelatedAlertId().getAlertId().equals(alert.getAlertId()));
        situation = !associatedAlerts.isEmpty();
    }

    public void removeRelatedAlertWithId(Long relatedAlertId) {
        associatedAlerts.removeIf(associatedAlert -> associatedAlert.getRelatedAlertId().getAlertId().equals(relatedAlertId));
        situation = !associatedAlerts.isEmpty();
    }

    @Transient
    public Set<Long> getRelatedSituationIds() {
        return getRelatedSituations().stream()
                .map(Alert::getAlertId)
                .collect(Collectors.toSet());
    }

    public void setRelatedSituations(Set<Alert> alerts) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Alert alert = (Alert) o;
        return alertId != null && Objects.equals(alertId, alert.alertId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
