package org.opennms.horizon.inventory.service.taskset.monitor;

import java.util.Map;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.opennms.icmp.contract.IcmpMonitorRequest;
import org.opennms.icmp.contract.IcmpMonitorRequest.Builder;
import org.springframework.stereotype.Component;

@Component
public class IcmpMonitorConfigParser extends SimpleParser<IcmpMonitorRequest> {

    public static final String ICMP_MONITOR = "ICMPMonitor";

    public IcmpMonitorConfigParser() {
        super(ICMP_MONITOR);
    }

    @Override
    public IcmpMonitorRequest parse(Map<String, String> input) {
        Builder builder = IcmpMonitorRequest.newBuilder();
        map(input, "ipAddress", "127.0.0.1", builder::setHost);
        map(input, "timeout", TaskUtils.ICMP_DEFAULT_TIMEOUT_MS, builder::setTimeout);
        map(input, "dscp", TaskUtils.ICMP_DEFAULT_DSCP, builder::setDscp);
        map(input, "allowFragmentation", TaskUtils.ICMP_DEFAULT_ALLOW_FRAGMENTATION, builder::setAllowFragmentation);
        map(input, "packetSize", TaskUtils.ICMP_DEFAULT_PACKET_SIZE, builder::setPacketSize);
        map(input, "retries", TaskUtils.ICMP_DEFAULT_RETRIES, builder::setRetries);
        return builder.build();
    }

}
