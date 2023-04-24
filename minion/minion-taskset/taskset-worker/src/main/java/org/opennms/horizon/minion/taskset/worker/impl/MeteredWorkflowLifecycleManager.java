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

package org.opennms.horizon.minion.taskset.worker.impl;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.Collections;
import java.util.Map;

import org.opennms.horizon.minion.taskset.worker.TaskSetLifecycleManager;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.taskset.contract.TaskSet;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

public class MeteredWorkflowLifecycleManager implements TaskSetLifecycleManager, MetricSet {

  private final IpcIdentity identity;
  private final TaskSetLifecycleManager delegate;
  private final Counter counter = new Counter();

  public MeteredWorkflowLifecycleManager(IpcIdentity identity, TaskSetLifecycleManager delegate) {
    this.identity = identity;
    this.delegate = delegate;
  }

  @Override
  public int deploy(TaskSet taskSet) {
    int size = taskSet.getTaskDefinitionList().size();
    int deployed = delegate.deploy(taskSet);
    counter.inc(size - deployed);
    return deployed;
  }

  @Override
  public TaskSet getDeployedTaskSet() {
    return delegate.getDeployedTaskSet();
  }

    @Override
  public Map<String, Metric> getMetrics() {
    return Collections.singletonMap(name("minion", identity.getLocation(), identity.getId(), "taskset"), counter);
  }

}
