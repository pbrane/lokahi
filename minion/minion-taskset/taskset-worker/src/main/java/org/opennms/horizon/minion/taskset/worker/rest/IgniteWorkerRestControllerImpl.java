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

package org.opennms.horizon.minion.taskset.worker.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.ServiceDescriptor;

@AllArgsConstructor
@Slf4j
public class IgniteWorkerRestControllerImpl implements IgniteWorkerRestController {

    //TODO: should this be injected through ignite annotation?
    private Ignite ignite;

    @Override
    public Response reportServiceDeploymentMetrics(boolean verbose) {
        Map<String, Object> result = calculateServiceDeploymentMetrics(verbose);

        return Response.ok(result).build();
    }

//========================================
// Internals
//----------------------------------------

    private Map<String, Object> calculateServiceDeploymentMetrics(boolean includeByService) {
        Map<String, Integer> countsByIgniteNode = new HashMap<>();
        Map<String, Integer> countsByService = new HashMap<>();
        AtomicInteger total = new AtomicInteger(0);

        Collection<ServiceDescriptor> serviceDescriptors = ignite.services().serviceDescriptors();
        serviceDescriptors.forEach(serviceDescriptor -> {
            Map<UUID, Integer> topo = serviceDescriptor.topologySnapshot();
            AtomicInteger subtotal = new AtomicInteger(0);

            for (Map.Entry<UUID, Integer> topoEntry : topo.entrySet()) {
                countsByIgniteNode.compute(String.valueOf(topoEntry.getKey()), (key, curVal) -> {

                    total.addAndGet(topoEntry.getValue());
                    subtotal.addAndGet(topoEntry.getValue());

                    if (curVal != null) {
                        return curVal + topoEntry.getValue();
                    } else {
                        return topoEntry.getValue();
                    }
                });
            }

            countsByService.put(serviceDescriptor.name(), subtotal.get());
        });

        // Sort
        Map<String, Integer> sortedCountsByIgniteNode = new TreeMap<>(countsByIgniteNode);
        Map<String, Integer> sortedServices = new TreeMap<>(countsByService);

        Map<String, Object> top = new TreeMap<>();
        top.put("countsByIgniteNode", sortedCountsByIgniteNode);

        if (includeByService) {
            top.put("countsByService", sortedServices);
        }

        top.put("total", total.get());
        top.put("serviceCount", serviceDescriptors.size());

        return top;
    }
}
