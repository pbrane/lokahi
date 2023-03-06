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
 * This interface provide functions that should be called
 * immediately after changing the alert entities while maintaining
 * an open transaction.
 *
 * The implementation should in turn notify any interested listeners
 * i.e. northbounders, correlation engines, etc... about the state change.
 *
 * The implementation should be thread safe.
 *
 * @author jwhite
 */
public interface AlertEntityNotifier {

    void didCreateAlert(Alert alert);

    void didUpdateAlertWithReducedEvent(Alert alert);

    void didAcknowledgeAlert(Alert alert, String previousAckUser, Date previousAckTime);

    void didUnacknowledgeAlert(Alert alert, String previousAckUser, Date previousAckTime);

    void didUpdateAlertSeverity(Alert alert, AlertSeverity previousSeverity);

    void didArchiveAlert(Alert alert, String previousReductionKey);

    void didDeleteAlert(Alert alert);

    void didUpdateStickyMemo(Alert alert, String previousBody, String previousAuthor, Date previousUpdated);

    void didUpdateReductionKeyMemo(Alert alert, String previousBody, String previousAuthor, Date previousUpdated);

    void didDeleteStickyMemo(Alert alert, Memo memo);

    void didUpdateLastAutomationTime(Alert alert, Date previousLastAutomationTime);

    void didUpdateRelatedAlerts(Alert alert, Set<Alert> previousRelatedAlerts);
}
