/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.Objects;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * <p> Entity to store situations and their associated (related) alerts with other details like mappedTime </p>
 */
@Entity
//TODO:MMF need to fix this
//@Table(name = "alert_association", uniqueConstraints={@UniqueConstraint(columnNames={"situation_alert_id", "related_alert_id"})})
@Table(name = "alert_association")
@Getter
@Setter
public class AlertAssociation extends TenantAwareEntity implements Serializable {

    private static final long serialVersionUID = 4115687014888009683L;

    @Id
    @SequenceGenerator(name="alertAssociationSequence", sequenceName="alertAssociationNxtId", allocationSize = 1)
    @GeneratedValue(generator="alertAssociationSequence")
    @Column(nullable=false)
    private Long alertAssociationId;

    @ManyToOne
    @JoinColumn(name="situation_alert_id")
    private Alert situationAlertId;

    @OneToOne
    @JoinColumn(name="related_alert_id")
    private Alert relatedAlertId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date mappedTime;


    public AlertAssociation() {
    }

    public AlertAssociation(Alert situationAlertId, Alert relatedAlertId) {
        this(situationAlertId, relatedAlertId, new Date());
    }

    public AlertAssociation(Alert situationAlertId, Alert relatedAlertId, Date mappedTime) {
        this.mappedTime = mappedTime;
        this.situationAlertId = situationAlertId;
        this.relatedAlertId = relatedAlertId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("situation", getSituationAlertId().getAlertId())
                .add("alert", getRelatedAlertId().getAlertId())
                .add("time", getMappedTime())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertAssociation that = (AlertAssociation) o;
        return Objects.equals(situationAlertId, that.situationAlertId) &&
                Objects.equals(relatedAlertId, that.relatedAlertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(situationAlertId, relatedAlertId);
    }
}
