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

package org.opennms.horizon.minion.taskset.ipc.internal;

import org.opennms.horizon.shared.ipc.sink.aggregation.IdentityAggregationPolicy;
import org.opennms.horizon.shared.ipc.sink.api.AggregationPolicy;
import org.opennms.horizon.shared.ipc.sink.api.AsyncPolicy;
import org.opennms.horizon.shared.ipc.sink.api.SinkModule;
import org.opennms.taskset.contract.TaskSetResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sink Module for processing TaskSetResults.  Note this is used in the communication between the Minion and
 * Minion Gateway, so Tenant IDs are not explicitly handled here.
 */
public class TaskSetResultsSinkModule implements SinkModule<TaskSetResults, TaskSetResults> {

  public static final String MODULE_ID = "task-set-result";
  private final Logger logger = LoggerFactory.getLogger(TaskSetResultsSinkModule.class);

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public int getNumConsumerThreads() {
    return 0;
  }

  @Override
  public byte[] marshal(TaskSetResults resultsMessage) {
    try {
      return resultsMessage.toByteArray();
    } catch (Exception e) {
      logger.warn("Error while marshalling message {}.", resultsMessage, e);
      return new byte[0];
    }
  }

  @Override
  public TaskSetResults unmarshal(byte[] bytes) {
    try {
      return TaskSetResults.parseFrom(bytes);
    } catch (Exception e) {
      logger.warn("Error while unmarshalling message.", e);
      return null;
    }
  }

  @Override
  public byte[] marshalSingleMessage(TaskSetResults resultsMessage) {
    return marshal(resultsMessage);
  }

  @Override
  public TaskSetResults unmarshalSingleMessage(byte[] bytes) {
    return unmarshal(bytes);
  }

  @Override
  public AggregationPolicy<TaskSetResults, TaskSetResults, ?> getAggregationPolicy() {
    return new IdentityAggregationPolicy<>();
  }

  @Override
  public AsyncPolicy getAsyncPolicy() {
    return new AsyncPolicy() {
      public int getQueueSize() {
        return 10;
      }

      public int getNumThreads() {
        return 10;
      }

      public boolean isBlockWhenFull() {
        return true;
      }
    };
  }

}
