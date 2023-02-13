/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.flows.adapter.ipfix;

import java.util.Objects;

import org.opennms.horizon.minion.flows.adapter.common.AdapterFactory;
import org.opennms.horizon.minion.flows.adapter.common.Adapter;
import org.opennms.horizon.minion.flows.parser.TelemetryRegistry;
import org.opennms.sink.flows.contract.AdapterConfig;

public class IpfixAdapterFactory implements AdapterFactory {
    private final TelemetryRegistry telemetryRegistry;

    public IpfixAdapterFactory(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
        telemetryRegistry.addAdapterFactory(this);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return IpfixAdapter.class;
    }


    @Override
    public Adapter createBean(final AdapterConfig adapterConfig) {
        Objects.requireNonNull(this.telemetryRegistry);
        return new IpfixAdapter(adapterConfig,
                                this.telemetryRegistry);
    }
}
