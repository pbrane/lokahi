/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
package org.opennms.horizon.graph.rest.internal;

import org.opennms.horizon.graph.api.service.GraphService;
import org.opennms.horizon.graph.rest.GraphRestService;
import org.opennms.horizon.graph.rest.renderer.JsonGraphRenderer;
import org.opennms.horizon.shared.dto.graph.GraphContainerInfo;
import org.osgi.framework.BundleContext;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

@Path("graphs")
public class GraphRestServiceImpl implements GraphRestService {

//    private final GraphService graphService;
//
//    private final BundleContext bundleContext;
//
//    public GraphRestServiceImpl(GraphService graphService,
//                                BundleContext bundleContext) {
//        this.graphService = Objects.requireNonNull(graphService);
//        this.bundleContext = Objects.requireNonNull(bundleContext);
//    }

    @Override
    public Response listContainerInfo() {
//        final List<GraphContainerInfo> graphContainerInfos = graphService.getGraphContainerInfos();
//        if (graphContainerInfos.isEmpty()) {
//            return Response.noContent().build();
//        }

        return Response.ok("container info - inside core").build();
    }

//    private String render(List<GraphContainerInfo> infos) {
//        return new JsonGraphRenderer(bundleContext).render(infos);
//    }
}
