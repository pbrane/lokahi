/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alarmservice.api;


import org.opennms.horizon.alarms.proto.Alarm;

/**
 * Used to be notified of updates/changes made to the set of alarms.
 *
 * Implementation should register the listeners with the {@link AlarmService} to receive callbacks.
 *
 * Listeners are invoked serially and the implementors should avoid blocking when possible.
 */
public interface AlarmLifecyleListener {
    /**
     * Called when an alarm has been created or updated.
     *
     * @param alarm a newly created or updated alarm
     */
    void handleNewOrUpdatedAlarm(Alarm alarm);

    /**
     * Called when an alarm has been deleted.
     *
     * @param alarm the deleted alarm
     */
    void handleDeletedAlarm(Alarm alarm);
}
