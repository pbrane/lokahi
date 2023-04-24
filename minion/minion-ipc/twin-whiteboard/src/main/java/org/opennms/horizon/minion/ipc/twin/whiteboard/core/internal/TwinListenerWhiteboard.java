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

package org.opennms.horizon.minion.ipc.twin.whiteboard.core.internal;

import static org.opennms.horizon.minion.ipc.twin.api.TwinListener.MESSAGE_LISTENER_TOPIC;

import com.savoirtech.eos.pattern.whiteboard.AbstractWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;
import org.opennms.horizon.minion.ipc.twin.api.TwinListener;
import org.opennms.horizon.minion.ipc.twin.api.TwinSubscriber;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwinListenerWhiteboard extends AbstractWhiteboard<TwinListener, Closeable> {

  private final Logger logger = LoggerFactory.getLogger(TwinListenerWhiteboard.class);
  private final TwinSubscriber twinSubscriber;

  public TwinListenerWhiteboard(BundleContext bundleContext, TwinSubscriber twinSubscriber) {
    super(bundleContext, TwinListener.class);
    this.twinSubscriber = twinSubscriber;
  }

  @Override
  protected Closeable addService(TwinListener service, ServiceProperties props) {
    Class<?> payload = service.getType();
    String subscriberKey = props.getProperty(MESSAGE_LISTENER_TOPIC);

    if (payload == null) {
      logger.warn("Subscriber {} does not specify proper payload type, ignoring", service);
      return null;
    }
    if (subscriberKey == null) {
      logger.warn("Subscriber {} for payload {} does not specify proper subscriber key, ignoring", service, payload.getName());
      return null;
    }

    return twinSubscriber.subscribe(subscriberKey, payload, new Consumer() {
      @Override
      public void accept(Object message) {
        if (payload.isInstance(message)) {
          service.accept(message);
        } else {
          logger.warn("Message {} is not a valid object required by subscriber {}.", message, subscriberKey);
        }
      }

      @Override
      public String toString() {
        return "closeable consumer for " + service;
      }
    });
  }

  @Override
  protected void removeService(TwinListener service, Closeable tracked) {
    try {
      tracked.close();
    } catch (IOException e) {
      logger.warn("Error while closing up grpc subscription for {}", service, e);
    }
  }

}
