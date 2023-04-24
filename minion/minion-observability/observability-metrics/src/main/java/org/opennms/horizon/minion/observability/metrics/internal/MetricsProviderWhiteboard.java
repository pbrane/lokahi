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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.savoirtech.eos.pattern.whiteboard.AbstractWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import org.opennms.horizon.minion.observability.metrics.MetricsProvider;
import org.osgi.framework.BundleContext;

public class MetricsProviderWhiteboard extends AbstractWhiteboard<MetricsProvider, MetricRegistry> implements
    MetricRegistryListener {

  private final MetricRegistry metricRegistry;

  public MetricsProviderWhiteboard(BundleContext bundleContext) {
    this(new MetricRegistry(), bundleContext);
  }

  public MetricsProviderWhiteboard(MetricRegistry metricRegistry, BundleContext bundleContext) {
    super(bundleContext, MetricsProvider.class);
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected MetricRegistry addService(MetricsProvider service, ServiceProperties props) {
    MetricRegistry registry = service.getMetrics();
    if (registry != null) {
      registry.addListener(this);
      return registry;
    }
    return null;
  }

  @Override
  protected void removeService(MetricsProvider service, MetricRegistry tracked) {
    for (String metric : tracked.getMetrics().keySet()) {
      metricRegistry.remove(metric);
    }
  }

  // listener stuff
  @Override
  public void onGaugeAdded(String name, Gauge<?> gauge) {
    register(name, gauge);
  }

  @Override
  public void onGaugeRemoved(String name) {
    remove(name);
  }

  @Override
  public void onCounterAdded(String name, Counter counter) {
    register(name, counter);
  }

  @Override
  public void onCounterRemoved(String name) {
    remove(name);
  }

  @Override
  public void onHistogramAdded(String name, Histogram histogram) {
    register(name, histogram);
  }

  @Override
  public void onHistogramRemoved(String name) {
    remove(name);
  }

  @Override
  public void onMeterAdded(String name, Meter meter) {
    register(name, meter);
  }

  @Override
  public void onMeterRemoved(String name) {
    remove(name);
  }

  @Override
  public void onTimerAdded(String name, Timer timer) {
    register(name, timer);
  }

  @Override
  public void onTimerRemoved(String name) {
    remove(name);
  }

  private void register(String name, Metric metric) {
    metricRegistry.register(name, metric);
  }

  private void remove(String name) {
    metricRegistry.remove(name);
  }
}
