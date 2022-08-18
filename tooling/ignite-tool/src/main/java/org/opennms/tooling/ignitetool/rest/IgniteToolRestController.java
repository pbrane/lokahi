package org.opennms.tooling.ignitetool.rest;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.services.ServiceDescriptor;
import org.opennms.taskset.model.TaskSet;
import org.opennms.taskset.service.api.TaskSetPublisher;
import org.opennms.taskset.service.model.LocatedTaskSet;
import org.opennms.tooling.ignitetool.message.IgniteMessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/ignite")
public class IgniteToolRestController {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(IgniteToolRestController.class);

    private Logger log = DEFAULT_LOGGER;

    @Autowired
    private Ignite ignite;

    @Autowired
    private IgniteMessageConsumerManager igniteMessageConsumerManager;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping(path = "/service-deployment/metrics", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Map<String, Object> reportServiceDeploymentMetrics(boolean verbose) {
        Map<String, Object> result = calculateServiceDeploymentMetrics(verbose);

        return result;
    }

    @PostMapping(path = "/message/{topic}")
    public void postMessageToIgniteTopic(@PathVariable("topic") String topic, @RequestBody String content) {
        ignite.message().send(topic, content);
    }

    @PutMapping(path = "/message/{topic}/listener")
    public void createMessageListener(@PathVariable("topic") String topic) {
        igniteMessageConsumerManager.startListenMessages(topic, (msg) -> this.igniteMessageLogger(topic, msg));
    }

    @DeleteMapping(path = "/message/{topic}/listener")
    public void removeMessageListener(@PathVariable("topic") String topic) {
        igniteMessageConsumerManager.stopListenMessages(topic);
    }

    @PostMapping(path = "/task-set/{location}", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void publishTaskSet(@PathVariable("location") String location, @RequestBody TaskSet taskSet) {
        LocatedTaskSet locatedTaskSet = new LocatedTaskSet(location, taskSet);

        log.info("Publishing task set: location={}; num-task={}", location, Optional.ofNullable(taskSet.getTaskDefinitionList()).map(Collection::size).orElse(0));

        ignite.message().send(TaskSetPublisher.TASK_SET_TOPIC, locatedTaskSet);
    }

    @GetMapping(path = "/topology/{version}")
    public Map getTopology(@PathVariable("version") String version) {
        long versionNumber = parseTopologyVersion(version);

        Collection<ClusterNode> topology = ignite.cluster().topology(versionNumber);

        Map result = new TreeMap();
        result.put("topologyVersion", versionNumber);
        result.put("detail", topology);
        result.put("summary", summarizeTopology(topology));

        return result;
    }

//========================================
// Internals
//----------------------------------------

    private long parseTopologyVersion(String versionString) {
        long versionNumber;
        if (
                (versionString == null) ||
                (versionString.isEmpty()) ||
                (versionString.equalsIgnoreCase("latest")) ||
                (versionString.equals("-"))
        ) {
            versionNumber = ignite.cluster().topologyVersion();
        } else {
            versionNumber = Long.parseLong(versionString);
        }

        return versionNumber;
    }

    private String formatElapsedTime(long firstTimestamp, long secondTimestamp) {
        long diffNano = secondTimestamp - firstTimestamp;
        long diffSec = diffNano / 1000000000L;
        long diffRemainingMilli = ( diffNano / 1000000L ) % 1000L;

        return diffSec + "s " + diffRemainingMilli + "ms";
    }

    private Map<String, Object> calculateServiceDeploymentMetrics(boolean includeByService) {
        Map<String, Integer> countsByIgniteNode = new HashMap<>();
        Map<String, Integer> countsByService = new HashMap<>();
        AtomicInteger total = new AtomicInteger(0);

        Collection<ServiceDescriptor> serviceDescriptors = ignite.services().serviceDescriptors();
        serviceDescriptors.forEach(serviceDescriptor -> {
            Map<UUID, Integer> topo = serviceDescriptor.topologySnapshot();
            AtomicInteger subtotal = new AtomicInteger(0);

            for (Map.Entry<UUID, Integer> topoEntry : topo.entrySet()) {
                countsByIgniteNode.compute(String.valueOf(topoEntry.getKey()), (key, curVal) -> {

                    total.addAndGet(topoEntry.getValue());
                    subtotal.addAndGet(topoEntry.getValue());

                    if (curVal != null) {
                        return curVal + topoEntry.getValue();
                    } else {
                        return topoEntry.getValue();
                    }
                });
            }

            countsByService.put(serviceDescriptor.name(), subtotal.get());
        });

        // Sort
        Map<String, Integer> sortedCountsByIgniteNode = new TreeMap<>(countsByIgniteNode);
        Map<String, Integer> sortedServices = new TreeMap<>(countsByService);

        Map<String, Object> top = new TreeMap<>();
        top.put("countsByIgniteNode", sortedCountsByIgniteNode);

        if (includeByService) {
            top.put("countsByService", sortedServices);
        }

        top.put("total", total.get());
        top.put("serviceCount", serviceDescriptors.size());

        return top;
    }

    private void igniteMessageLogger(String topic, Object content) {
        log.info("MESSAGE RECEIVED: topic={}; content={}", topic, content);
    }

    private Map summarizeTopology(Collection<ClusterNode> nodes) {
        Map result = new TreeMap();

        result.put("node-count", nodes.size());

        Map summaryPerNode = new TreeMap();
        nodes.forEach(node -> {
            Map oneNodeSummary = new TreeMap();
            oneNodeSummary.put("addresses", node.addresses());
            oneNodeSummary.put("hostnames", node.hostNames());

            summaryPerNode.put(node.id(), oneNodeSummary);
        });

        result.put("nodes", summaryPerNode);

        return result;
    }
}
