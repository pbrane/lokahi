package org.opennms.core.ipc.grpc.server.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinionInfo {
    private String id;
    private String location;
}
