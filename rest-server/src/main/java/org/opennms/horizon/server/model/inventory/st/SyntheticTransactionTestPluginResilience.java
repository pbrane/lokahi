package org.opennms.horizon.server.model.inventory.st;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyntheticTransactionTestPluginResilience {

    private long timeout;
    private int retries;

}
