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

package org.opennms.horizon.minion.flows.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.opennms.horizon.minion.flows.adapter.common.Adapter;
import org.opennms.horizon.minion.flows.parser.flowmessage.NetflowVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterHolder {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterHolder.class);

    private final Map<String, Adapter> adapterMap = new HashMap<>();

    public boolean containsKey(String key) {
        return adapterMap.containsKey(key);
    }

    public Adapter findByNetflowVersion(NetflowVersion netflowVersion) {
        Optional<Adapter> adapter = adapterMap.values().stream()
            .filter(v -> v.getNetflowVersion().equals(netflowVersion)).findFirst();
        if (adapter.isPresent()) {
            return adapter.get();
        } else {
            LOG.error("For the given Netflow Version '{}' there is not any adapter available; list of available adapters=[{}]",
                netflowVersion, adapterMap.values());
            return null;
        }
    }

    public Adapter get(String adapterName) {
        return adapterMap.get(adapterName);
    }

    public void put(String name, Adapter adapter) {
        adapterMap.put(name, adapter);
        LOG.info("Adapter {} added into holder.", adapter.getClassName());
    }

    public void clearAll() {
        adapterMap.clear();
    }

    public int size() {
        return adapterMap.size();
    }

}
