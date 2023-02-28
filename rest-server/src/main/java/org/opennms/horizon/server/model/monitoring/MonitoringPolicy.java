package org.opennms.horizon.server.model.monitoring;

import lombok.Getter;
import lombok.Setter;
import org.opennms.horizon.server.model.inventory.tag.Tag;

import java.util.List;

@Getter
@Setter
public class MonitoringPolicy {

    private long id;
    private String tenantId;
    private String name;
    private List<Tag> nodeTags;
    private String Location;

    //private String managementInformationBase; // MiB
    //private String notify;
}
