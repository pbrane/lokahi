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
package org.opennms.horizon.minion.observability.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.savoirtech.eos.pattern.whiteboard.AbstractWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import org.osgi.framework.BundleContext;

public class MetricsWhiteboard extends AbstractWhiteboard<MetricSet, MetricSet> {

  private final MetricRegistry metricRegistry;

  public MetricsWhiteboard(BundleContext bundleContext) {
    this(new MetricRegistry(), bundleContext);
  }

  public MetricsWhiteboard(MetricRegistry metricRegistry, BundleContext bundleContext) {
    super(bundleContext, MetricSet.class);
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected MetricSet addService(MetricSet service, ServiceProperties props) {
    metricRegistry.registerAll(service);
    return service;
  }

  @Override
  protected void removeService(MetricSet service, MetricSet tracked) {
    for (String metric : service.getMetrics().keySet()) {
      metricRegistry.remove(metric);
    }
  }
}
