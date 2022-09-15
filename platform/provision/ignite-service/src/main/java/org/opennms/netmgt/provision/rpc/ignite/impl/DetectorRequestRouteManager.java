package org.opennms.netmgt.provision.rpc.ignite.impl;

import org.apache.ignite.Ignite;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

// TBD888: push routing down into gateways only (LATER)
@Deprecated
public class DetectorRequestRouteManager {

    public final String MINION_ROUTE_MAP_CACHENAME = "MINION-ROUTE-MAP";
    public final String LOCATION_ROUTE_MAP_CACHENAME = "LOCATION-ROUTE-MAP";

    private final IgniteClient igniteClient;

    private final Map<String, AtomicInteger> locationCycle = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> systemIdCycle = new ConcurrentHashMap<>();

//========================================
// Constructor
//----------------------------------------

    public DetectorRequestRouteManager(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
    }

//========================================
// Lookups
//----------------------------------------

    public UUID findNodeIdToUseForLocation(String location) {
        Object nodeList;

        ClientCache<Object, Object> cache = igniteClient.cache(LOCATION_ROUTE_MAP_CACHENAME);

        if (cache != null) {
            nodeList = cache.get(location);

            if (nodeList != null) {
                //
                // PERFORMANCE NOTE: if the number of gateway nodes per location gets large (e.g. 1000) then it may be
                //  necessary to change this from a List to another structure for faster updates.  Not anticipating that
                //  many gateway nodes per location at the time of this writing - on the order of 3-10 seems likely.
                //
                List<UUID> nodes = (List<UUID>) nodeList;

                if (!nodes.isEmpty()) {
                    locationCycle.putIfAbsent(location, new AtomicInteger(0));
                    int cycleNumber = locationCycle.get(location).getAndIncrement();
                    return nodes.get(cycleNumber % nodes.size());
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public UUID findNodeIdToUseForSystemId(String systemId) {
        Object nodeList;

        nodeList = igniteClient.cache(MINION_ROUTE_MAP_CACHENAME).get(systemId);

        if (nodeList != null)  {
            //
            // PERFORMANCE NOTE: if the number of gateway nodes per location gets large (e.g. 1000) then it may be
            //  necessary to change this from a List to another structure for faster updates.  Not anticipating that
            //  many gateway nodes per location at the time of this writing - on the order of 3-10 seems likely.
            //
            List<UUID> nodes = (List<UUID>) nodeList;

            if (! nodes.isEmpty()) {
                systemIdCycle.putIfAbsent(systemId, new AtomicInteger(0));
                int cycleNumber = systemIdCycle.get(systemId).getAndIncrement();

                return nodes.get(cycleNumber % nodes.size());
            } else {
                return null;
            }
        }
        return null;
    }

}
