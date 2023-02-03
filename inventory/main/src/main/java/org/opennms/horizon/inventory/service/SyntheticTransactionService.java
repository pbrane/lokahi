package org.opennms.horizon.inventory.service;


import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.horizon.inventory.component.MinionRpcClient;
import org.opennms.horizon.inventory.dto.SyntheticTestCreateDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestLocationConfigurationDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestPluginConfigurationDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestPluginResilienceDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestStatusDTO;
import org.opennms.horizon.inventory.dto.SyntheticTestStatusDTO.Builder;
import org.opennms.horizon.inventory.dto.SyntheticTransactionCreateDTO;
import org.opennms.horizon.inventory.dto.SyntheticTransactionDTO;
import org.opennms.horizon.inventory.dto.SyntheticTransactionTestLocationRequestDTO;
import org.opennms.horizon.inventory.dto.SyntheticTransactionTestRequestDTO;
import org.opennms.horizon.inventory.dto.TenantedId;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.SyntheticTest;
import org.opennms.horizon.inventory.model.SyntheticTestConfig;
import org.opennms.horizon.inventory.model.SyntheticTransaction;
import org.opennms.horizon.inventory.model.TenantAware;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.SyntheticTestRepository;
import org.opennms.horizon.inventory.repository.SyntheticTransactionRepository;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.opennms.horizon.inventory.service.taskset.monitor.ConfigFactory;
import org.opennms.horizon.inventory.service.taskset.monitor.ConfigParser;
import org.opennms.horizon.inventory.service.taskset.publisher.TaskSetPublisher;
import org.opennms.taskset.contract.MonitorResponse;
import org.opennms.taskset.contract.Resilience;
import org.opennms.taskset.contract.SyntheticTransactionMetadata;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskMetadata;
import org.opennms.taskset.contract.TaskResult;
import org.opennms.taskset.contract.TaskType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyntheticTransactionService  {

    private final TaskSetPublisher taskSetPublisher;

    private final SyntheticTransactionRepository syntheticTransactionRepository;
    private final SyntheticTestRepository syntheticTestRepository;
    private final MonitoringLocationRepository monitoringLocationRepository;

    private final MinionRpcClient rpcClient;

    private final ConfigFactory configFactory;

    @Transactional
    public SyntheticTransactionDTO store(SyntheticTransactionCreateDTO request) {
        SyntheticTransaction transaction = new SyntheticTransaction();
        transaction.setTenantId(request.getTenantId());
        transaction.setLabel(request.getLabel());

        return transactionFromModel(syntheticTransactionRepository.save(transaction));
    }

    public List<SyntheticTransactionDTO> getSyntheticTransactionsByTenantId(String tenantId) {
        List<SyntheticTransactionDTO> list = new ArrayList<>();
        for (SyntheticTransaction syntheticTransaction : syntheticTransactionRepository.findByTenantId(tenantId)) {
            list.add(transactionFromModel(syntheticTransaction));
        }
        return list;
    }

    @Transactional
    public void deleteTransaction(String tenant, long id) {
        syntheticTransactionRepository.deleteByTenantIdAndId(tenant, id);
    }

    public Optional<SyntheticTransactionDTO> getSyntheticTransaction(TenantedId tenantId) {
        return syntheticTransactionRepository.findByTenantIdAndId(tenantId.getTenant(), tenantId.getId())
            .map(this::transactionFromModel);
    }

    public List<SyntheticTestDTO> getSyntheticTransactionsTests(TenantedId tenantId) {
        return syntheticTestRepository.findByTenantIdAndSyntheticTransactionId(tenantId.getTenant(), tenantId.getId()).stream()
            .map(this::testFromModel)
            .collect(Collectors.toList());
    }

    @Transactional
    public SyntheticTestDTO storeTest(SyntheticTestCreateDTO request) {
        TenantedId transactionId = request.getSyntheticTransactionId();

        Optional<SyntheticTransaction> transaction = syntheticTransactionRepository.findByTenantIdAndId(transactionId.getTenant(), transactionId.getId());
        if (transaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction does not exist");
        }

        SyntheticTest test = new SyntheticTest();
        test.setSyntheticTransaction(transaction.get());
        test.setTenantId(transactionId.getTenant());
        test.setLabel(request.getLabel());
        test.setSchedule(request.getSchedule());

        SyntheticTestPluginResilienceDTO resilience = request.getPluginConfiguration().getResilience();
        test.setPluginName(request.getPluginConfiguration().getPluginName());
        test.setRetries(resilience.getRetries());
        test.setTimeout(resilience.getTimeout());
        Map<String, SyntheticTestConfig> testConfig = new LinkedHashMap<>();
        Map<String, String> configMap = request.getPluginConfiguration().getConfigMap();
        for (Entry<String, String> entry : configMap.entrySet()) {
            SyntheticTestConfig config = new SyntheticTestConfig();
            config.setTenantId(transactionId.getTenant());
            config.setSyntheticTest(test);
            config.setParameter(entry.getKey());
            config.setValue(entry.getValue());
            testConfig.put(entry.getKey(), config);
        }
        test.setConfig(testConfig);

        List<MonitoringLocation> locations = monitoringLocationRepository.findByTenantIdAndLocationIn(transactionId.getTenant(), request.getLocationsList());
        if (locations.isEmpty() || locations.size() != request.getLocationsCount()) {
            throw new IllegalArgumentException("Invalid locations");
        }
        test.setLocations(locations);


        ConfigParser<?> parser = configFactory.createParser(test.getPluginName());
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported plugin " + test.getPluginName());
        }

        SyntheticTest syntheticTest = syntheticTestRepository.save(test);
        Message config = parser.parse(configMap);
        for (MonitoringLocation location : locations) {
            List<TaskDefinition> tasks = new ArrayList<>();

            String taskId = "synthetic/transaction:" + transactionId.getId() + "/test:" + test.getId() + "/" + test.getPluginName();

            TaskDefinition taskDefinition = createMonitoringTaskFromTest(taskId, syntheticTest, config, resilience);
            tasks.add(taskDefinition);

            taskSetPublisher.publishNewTasks(
                syntheticTest.getTenantId(), location.getLocation(), tasks
            );
        }

        return testFromModel(test);
    }

    @Transactional
    public void deleteTransactionTest(String tenant, long testId) {
        syntheticTestRepository.deleteByTenantIdAndId(tenant, testId);
    }

    private static TaskDefinition createMonitoringTaskFromTest(String taskId, SyntheticTest syntheticTest, Message config, SyntheticTestPluginResilienceDTO resilience) {
        TaskDefinition taskDefinition = TaskDefinition.newBuilder()
            .setType(TaskType.MONITOR)
            .setPluginName(syntheticTest.getPluginName())
            .setMetadata(TaskMetadata.newBuilder()
                .setSyntheticTransaction(SyntheticTransactionMetadata.newBuilder()
                    .setSyntheticTestId(syntheticTest.getId())
                    .setSyntheticTransactionId(syntheticTest.getSyntheticTransaction().getId())
                )
            ).setResilience(Resilience.newBuilder()
                .setTimeout(resilience.getTimeout())
                .setRetries(resilience.getRetries())
            ).setId(taskId)
            .setConfiguration(Any.pack(config))
            .setSchedule(TaskUtils.DEFAULT_SCHEDULE)
            .build();
        return taskDefinition;
    }

    private SyntheticTransactionDTO transactionFromModel(SyntheticTransaction model) {
        return SyntheticTransactionDTO.newBuilder()
            .setId(transportId(model))
            .setLabel(model.getLabel())
            .build();
    }


    private SyntheticTestDTO testFromModel(SyntheticTest model) {
        return SyntheticTestDTO.newBuilder()
            .setId(transportId(model))
            .setLabel(model.getLabel())
            .setPluginConfiguration(SyntheticTestPluginConfigurationDTO.newBuilder()
                .setPluginName(model.getPluginName())
                .setResilience(SyntheticTestPluginResilienceDTO.newBuilder()
                    .setRetries(model.getRetries())
                    .setTimeout(model.getTimeout())
                )
            )
            .setSyntheticTransactionId(model.getSyntheticTransaction().getId())
            .addAllLocations(Optional.ofNullable(model.getLocations()).orElse(Collections.emptyList()).stream()
                .map(MonitoringLocation::getLocation)
                .collect(Collectors.toList())
            )
            .build();
    }

    private static TenantedId transportId(TenantAware entity) {
        return TenantedId.newBuilder()
            .setTenant(entity.getTenantId())
            .setId(entity.getId())
            .build();
    }

    public CompletableFuture<Map<String, SyntheticTestStatusDTO>> executeTestInLocationsMatching(SyntheticTransactionTestRequestDTO request) {
        return executeTestInLocationsMatching(request.getId(), (monitoringLocation) -> true);
    }

    public CompletableFuture<SyntheticTestStatusDTO> executeLocation(SyntheticTransactionTestLocationRequestDTO request) {
        return executeTestInLocationsMatching(request.getId(), (monitoringLocation) -> monitoringLocation.getLocation().equals(request.getLocation()))
            .thenApply(result -> result.get(request.getLocation()));
    }

    public CompletableFuture<SyntheticTestStatusDTO> executeConfiguration(String tenantId, SyntheticTestLocationConfigurationDTO request) {
        SyntheticTestPluginConfigurationDTO configuration = request.getPluginConfiguration();
        ConfigParser<?> configParser = configFactory.createParser(configuration.getPluginName());
        if (configParser == null) {
            return CompletableFuture.completedFuture(SyntheticTestStatusDTO.newBuilder()
                .setReason("Unknown plugin")
                .build()
            );
        }
        Message message = configParser.parse(configuration.getConfigMap());

        Optional<String> location = monitoringLocationRepository.findByLocationAndTenantId(request.getLocation(), tenantId)
            .map(MonitoringLocation::getLocation);
        if (location.isEmpty()) {
            return CompletableFuture.completedFuture(SyntheticTestStatusDTO.newBuilder()
                .setReason("Unknown location")
                .build()
            );
        }

        return callMonitorInLocation(tenantId, location.get(), configuration.getPluginName(), message)
            .thenApply(response -> response.get(location.get()));
    }

    private CompletableFuture<Map<String, SyntheticTestStatusDTO>> executeTestInLocationsMatching(TenantedId testId, Predicate<MonitoringLocation> predicate) {
        Optional<SyntheticTest> syntheticTest = syntheticTestRepository.findById(testId.getId());
        if (syntheticTest.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown synthetic test"));
        }

        SyntheticTest test = syntheticTest.get();
        Map<String, String> config = test.getConfig().entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue()));

        List<MonitoringLocation> locations = Optional.ofNullable(test.getLocations()).orElse(Collections.emptyList()).stream()
            .filter(predicate)
            .toList();
        if (locations.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Test is not bound to any location or specified locations did not mathc"));
        }

        ConfigParser<?> parser = configFactory.createParser(test.getPluginName());
        if (parser == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unsupported plugin"));
        }
        Message pluginConfig = parser.parse(config);

        CompletableFuture<Map<String, SyntheticTestStatusDTO>> result = null;
        for (MonitoringLocation location : locations) {
            CompletableFuture<Map<String, SyntheticTestStatusDTO>> future = callMonitorInLocation(testId.getTenant(), location.getLocation(), test.getPluginName(), pluginConfig);

            if (result == null) {
                result = future.thenApply(response -> {
                    // wrap result in concurrent structure
                    Map<String, SyntheticTestStatusDTO> map = new ConcurrentHashMap<>();
                    map.putAll(response);
                    return map;
                });
            } else {
                result.thenCombine(future, (statuses, completeLocation) -> {
                    statuses.putAll(completeLocation);
                    return statuses;
                });
            }
        }

        return result;
    }

    // execute
    private CompletableFuture<Map<String, SyntheticTestStatusDTO>> callMonitorInLocation(String tenantId, String location, String pluginName, Message pluginConfig) {
        TaskDefinition taskDefinition = TaskDefinition.newBuilder()
            .setType(TaskType.MONITOR)
            .setPluginName(pluginName)
            .setConfiguration(Any.pack(pluginConfig))
            .build();
        RpcRequestProto rpcRequest = RpcRequestProto.newBuilder()
            .setRpcId(UUID.randomUUID().toString())
            .setLocation(location)
            .setModuleId("task")
            .setPayload(Any.pack(taskDefinition))
            .setExpirationTime(System.currentTimeMillis() + 30_000)
            .build();

        return rpcClient.sendRpcRequest(tenantId, rpcRequest)
            .thenApply(RpcResponseProto::getPayload)
            .thenApply(payload -> {
                Builder statusDTO = SyntheticTestStatusDTO.newBuilder();
                try {
                    TaskResult result = payload.unpack(TaskResult.class);
                    if (result.hasMonitorResponse()) {
                        MonitorResponse response = result.getMonitorResponse();
                        Optional.of(response.getResponseTimeMs()).ifPresent(statusDTO::setResponseTimeMs);
                        Optional.of(response.getReason()).ifPresent(statusDTO::setReason);
                    } else {
                        statusDTO.setReason("Unexpected answer");
                    }
                } catch (InvalidProtocolBufferException e) {
                    log.warn("Could not handle rpc request {} for tenant {}", rpcRequest, tenantId, e);
                    statusDTO.setReason("Failure while parsing monitor answer");
                }
                return ImmutableMap.of(location, statusDTO.build());
            });
    }

}
