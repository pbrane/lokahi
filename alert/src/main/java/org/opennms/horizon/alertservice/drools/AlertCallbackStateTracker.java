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

package org.opennms.horizon.alertservice.drools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.horizon.alertservice.api.AlertLifecycleListener;

import com.google.common.collect.ImmutableSet;

/**
 * This class can be used to help track callbacks issued via the {@link AlertLifecycleListener}
 * in order to help simplify possible synchronization logic in {@link AlertLifecycleListener#handleAlertSnapshot(List)}.
 *
 * @author jwhite
 */
public class AlertCallbackStateTracker {

    private final Set<Long> alertsUpdatesById = new HashSet<>();
    private final Set<String> alertsUpdatesByReductionKey = new HashSet<>();

    private final Set<Long> deletedAlertsByAlertId = new HashSet<>();
    private final Set<String> deletedAlertsByReductionKey = new HashSet<>();

    private final List<Set<?>> sets = Arrays.asList(alertsUpdatesById, alertsUpdatesByReductionKey,
            deletedAlertsByAlertId, deletedAlertsByReductionKey);

    private boolean trackAlerts = false;

    public synchronized void startTrackingAlerts() {
        trackAlerts = true;
    }

    public synchronized void trackNewOrUpdatedAlert(long alertId, String reductionKey) {
        if (!trackAlerts) {
            return;
        }
        alertsUpdatesById.add(alertId);
        alertsUpdatesByReductionKey.add(reductionKey);
    }

    public synchronized void trackDeletedAlert(long alertId, String reductionKey) {
        if (!trackAlerts) {
            return;
        }
        deletedAlertsByAlertId.add(alertId);
        deletedAlertsByReductionKey.add(reductionKey);
    }

    public synchronized void resetStateAndStopTrackingAlerts() {
        trackAlerts = false;
        sets.forEach(Set::clear);
    }

    // By ID

    public synchronized boolean wasAlertWithIdUpdated(long alertId) {
        return alertsUpdatesById.contains(alertId);
    }

    public synchronized boolean wasAlertWithIdDeleted(long alertId) {
        return deletedAlertsByAlertId.contains(alertId);
    }

    // By reduction key

    public synchronized boolean wasAlertWithReductionKeyUpdated(String reductionKey) {
        return alertsUpdatesByReductionKey.contains(reductionKey);
    }

    public synchronized boolean wasAlertWithReductionKeyDeleted(String reductionKey) {
        return deletedAlertsByReductionKey.contains(reductionKey);
    }

    public Set<Long> getUpdatedAlertIds() {
        return ImmutableSet.copyOf(alertsUpdatesById);
    }
}
