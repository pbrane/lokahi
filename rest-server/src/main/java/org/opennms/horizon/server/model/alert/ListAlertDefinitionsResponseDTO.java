package org.opennms.horizon.server.model.alert;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListAlertDefinitionsResponseDTO {
    // A possibly paginated list of Alerts that are associated with the calling tenant.
    private List<AlertDefinitionDTO> definitions;

    // Optional. A pagination token returned from a previous call to `ListAlertDefinitions`
    // that indicates where this listing should continue from.
    private String pageToken;
}
