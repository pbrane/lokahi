package org.opennms.horizon.minion.snmp;

import org.opennms.horizon.shared.snmp.SnmpUtils;
import org.opennms.horizon.shared.snmp.StrategyResolver;

public class UtilInitializer {

    public UtilInitializer(StrategyResolver strategyResolver) {
        SnmpUtils.setStrategyResolver(strategyResolver);
    }

}
