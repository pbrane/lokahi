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
import java.util.Set;

import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.Memo;
import org.opennms.horizon.alertservice.model.AlertSeverity;

/**
 * Used to get callbacks when alert entities are created, updated and/or deleted.
 *
 * @author jwhite
 */
public interface AlertEntityListener {

    void onAlertCreated(Alert alert);

    void onAlertUpdatedWithReducedEvent(Alert alert);

    void onAlertAcknowledged(Alert alert, String previousAckUser, Date previousAckTime);

    void onAlertUnacknowledged(Alert alert, String previousAckUser, Date previousAckTime);

    void onAlertSeverityUpdated(Alert alert, AlertSeverity previousSeverity);

    void onAlertArchived(Alert alert, String previousReductionKey);

    void onAlertDeleted(Alert alert);

    void onStickyMemoUpdated(Alert alert, String previousBody, String previousAuthor, Date previousUpdated);

    void onReductionKeyMemoUpdated(Alert alert, String previousBody, String previousAuthor, Date previousUpdated);

    void onStickyMemoDeleted(Alert alert, Memo memo);

    void onLastAutomationTimeUpdated(Alert alert, Date previousLastAutomationTime);

    void onRelatedAlertsUpdated(Alert alert, Set<Alert> previousRelatedAlerts);
}
