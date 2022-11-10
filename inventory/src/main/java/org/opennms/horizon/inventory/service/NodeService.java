package org.opennms.horizon.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.mapper.NodeMapper;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.Node;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NodeService {
    private final NodeRepository modelRepo;

    private final MonitoringLocationRepository monitoringLocationRepository;

    private final NodeMapper mapper;

    public NodeDTO saveNode(NodeDTO dto) {
        Node model = mapper.dtoToModel(dto);

        MonitoringLocation monitoringLocation = monitoringLocationRepository.getReferenceById(dto.getMonitoringLocationId());
        model.setMonitoringLocation(monitoringLocation);

        Node ret = modelRepo.save(model);
        return mapper.modelToDTO(ret);
    }

    public List<NodeDTO> findAllNodes() {
        List<Node> all = modelRepo.findAll();
        return all
            .stream()
            .map(mapper::modelToDTO)
            .collect(Collectors.toList());
    }

    public Optional<NodeDTO> findNode(long id) {
        Optional<Node> model = modelRepo.findById(id);
        Optional<NodeDTO> dto = Optional.empty();
        if (model.isPresent()) {
            dto = Optional.of(mapper.modelToDTO(model.get()));
        }
        return dto;
    }

    public List<NodeDTO> findByTenantId(String tenantId) {
        List<Node> all = modelRepo.findByTenantId(tenantId);
        return all
            .stream()
            .map(mapper::modelToDTO)
            .collect(Collectors.toList());
    }
}
