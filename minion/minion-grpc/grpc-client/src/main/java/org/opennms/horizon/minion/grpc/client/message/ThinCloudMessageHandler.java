/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.grpc.client.message;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.horizon.minion.grpc.CloudMessageHandler;
import org.opennms.horizon.shared.ipc.rpc.api.minion.CloudMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThinCloudMessageHandler implements CloudMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(ThinCloudMessageHandler.class);
    private final Set<CloudMessageReceiver> handlers = new CopyOnWriteArraySet<>();

    @Override
    public void handle(CloudToMinionMessage message) {
        if (handlers.isEmpty()) {
            logger.warn("No registered handlers for message {}", message);
            return;
        }

        for (CloudMessageReceiver handler : handlers) {
            if (handler.canHandle(message)) {
                handler.handle(message);
            }
        }
    }

    public void bind(CloudMessageReceiver handler) {
        if (handler != null) {
            handlers.add(handler);
            logger.info("Registered cloud message handler {}", handler);
        }
    }

    public void unbind(CloudMessageReceiver handler) {
        if (handler != null) {
            handlers.remove(handler);
            logger.info("Removing cloud message handler {}", handler);
        }
    }
}
