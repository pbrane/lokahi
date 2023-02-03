package org.opennms.horizon.server.model.inventory.st;

import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyntheticTransactionTestPluginConfiguration {

    @NotBlank
    private String pluginName;
    private Map<String, String> config;

    private SyntheticTransactionTestPluginResilience resilience;

}
