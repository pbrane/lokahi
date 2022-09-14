package org.opennms.horizon.provision.rest;

import com.google.gson.Gson;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.netmgt.provision.DetectorRequestExecutorBuilder;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.service.Provisioner;

@RequiredArgsConstructor
@Slf4j
public class ProvisionRestServiceImpl implements ProvisionRestService {
    private final Provisioner provisioner;
    private final SessionUtils sessionUtils;
    private Gson gson = new Gson();
    private final LocationAwareDetectorClient locationAwareDetectorClient;

    @Override
    public Response publishRequisition(String requisition) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                RequisitionDTO requisitionDTO = gson.fromJson(requisition, RequisitionDTO.class);
                String id = provisioner.publish(requisitionDTO);
                return Response.ok().entity(id).build();
            } catch (Exception e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        });
    }

    @Override
    public Response getRequisition(String requisitionName) {
        return this.sessionUtils.withReadOnlyTransaction(() -> {
            Optional<RequisitionDTO> data = provisioner.read(requisitionName);
            if (data.isPresent()) {
                log.info("Found the req {}", data);
                return Response.ok().entity(gson.toJson(data.get())).build();
            }
            else {
                return Response.status(Status.NOT_FOUND).build();
            }
        });
    }

    @Override
    public Response updateRequisition(String requisition) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                RequisitionDTO requisitionDTO = gson.fromJson(requisition, RequisitionDTO.class);
                String id = provisioner.update(requisitionDTO);
                return Response.ok().entity(id).build();
            } catch (Exception e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        });
    }

    @Override
    public Response deleteRequisition(String requisitionName) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                provisioner.delete(requisitionName);
                return Response.ok().build();
            }
            catch (Exception e) {
                return Response.noContent().build();
            }
        });
    }

    @Override
    public Response scanNodes() {
        return this.sessionUtils.withReadOnlyTransaction(() -> {
            provisioner.performNodeScan();
            return Response.ok().build();
        });
    }

    @Override
    public Response publishForeignSource(String foreignSource) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                ForeignSourceDTO foreignSourceDTO = gson.fromJson(foreignSource, ForeignSourceDTO.class);
                String id = provisioner.publish(foreignSourceDTO);
                return Response.ok().entity(id).build();
            } catch (Exception e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        });
    }

    @Override
    public Response runIgniteTest(String foreignSource) {
        DetectorRequestExecutorBuilder detectorRequestExecutorBuilder = locationAwareDetectorClient.detect()
            .withLocation("foo")
            .withServiceName("bar");
        CompletableFuture<Boolean> future = detectorRequestExecutorBuilder.build().execute();
        try {
            boolean results = future.get().booleanValue();
            if (results) {
                return Response.ok().build();
            }
            else {
                return Response.serverError().build();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
