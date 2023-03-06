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

package org.opennms.horizon.alertservice.api;

import java.util.Date;
import java.util.List;

import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.model.AlertDTO;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.events.proto.Event;

/**
 * This API is intended to provide RHS functionality for Drools Alertd and
 * Situation rules.
 */
public interface AlertService {

    AlertDTO clearAlert(Alert alert, Date now);

    AlertDTO clearAlert(Long alertId, Date now);

    AlertDTO deleteAlert(Long id);

    AlertDTO deleteAlert(Alert alert);

    AlertDTO unclearAlert(Alert alert, Date now);

    AlertDTO unclearAlert(Long alertId, Date now);

    AlertDTO escalateAlert(Alert alert, Date now);

    AlertDTO escalateAlert(Long alertId, Date now);

    AlertDTO acknowledgeAlert(Alert alert, Date now, String userId);

    AlertDTO acknowledgeAlert(Long alertId, Date now, String userId);

    AlertDTO unAcknowledgeAlert(Long alertId, Date now);

    AlertDTO unAcknowledgeAlert(Alert alert, Date now);

    AlertDTO setSeverity(Alert alert, AlertSeverity severity, Date now);

    AlertDTO setSeverity(Long alertId, AlertSeverity severity, Date now);

    List<AlertDTO> getAllAlerts(String tenantId);

    AlertDTO process(Event e);

    AlertDTO removeStickyMemo(long alertId);

    AlertDTO updateStickyMemo(Long alertId, String body);

}
