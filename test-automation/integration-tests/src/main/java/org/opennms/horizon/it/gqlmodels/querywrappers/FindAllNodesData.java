package org.opennms.horizon.it.gqlmodels.querywrappers;

import java.util.List;
import org.opennms.horizon.it.gqlmodels.LocationData;
import org.opennms.horizon.it.gqlmodels.NodeData;

public class FindAllNodesData {

    private Wrapper data;

    public Wrapper getData() {
        return data;
    }

    public void setData(Wrapper data) {
        this.data = data;
    }

    public static class Wrapper {
        private List<NodeData> findAllNodes;

        public List<NodeData> getFindAllNodes() {
            return findAllNodes;
        }

        public void setFindAllNodes(List<NodeData> findAllNodes) {
            this.findAllNodes = findAllNodes;
        }
    }
}
