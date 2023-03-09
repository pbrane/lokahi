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

package org.opennms.horizon.alarmservice.service;

import org.opennms.horizon.alarmservice.api.AlarmLifecyleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Helper class used to track and interact with {@link AlarmLifecyleListener}s.
 */
@Service
public class AlarmListenerRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmListenerRegistry.class);

    private final List<AlarmLifecyleListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(AlarmLifecyleListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlarmLifecyleListener listener) {
        listeners.remove(listener);
    }

    public void forEachListener(Consumer<AlarmLifecyleListener> callback) {
        for (AlarmLifecyleListener listener : listeners) {
            try {
                callback.accept(listener);
            } catch (Exception e) {
                LOG.error("Error occurred while invoking listener: {}. Skipping.", listener, e);
            }
        }
    }
}
