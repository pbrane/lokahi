package org.opennms.horizon.server.model.alert;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListAlertDefinitionsRequestDTO {
    // Optional. The maximum number of alerts to return in the response.
    private int pageSize;

    // Optional. A pagination token returned from a previous call to `listAlerts`
    // that indicates where this listing should continue from.
    private String pageToken;
}
