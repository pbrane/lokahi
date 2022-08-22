package org.opennms.horizon.graph.api.service;

import org.opennms.horizon.shared.dto.graph.GraphContainerInfo;

import java.util.List;

public interface GraphService {

    List<GraphContainerInfo> getGraphContainerInfos();
}
