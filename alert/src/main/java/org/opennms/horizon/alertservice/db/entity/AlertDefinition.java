package org.opennms.horizon.alertservice.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opennms.horizon.alerts.proto.AlertType;
import org.opennms.horizon.alerts.proto.ManagedObjectType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="alert_definition")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AlertDefinition extends TenantAwareEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7825119801706458618L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_definition_id")
    private long id;

    @NotNull
    @Column(name = "tenant_id")
    private String tenantId;

    @NotNull
    @Column(name = "uei")
    private String uei;

    @OneToMany(mappedBy = "id")
    private List<EventMatch> match = new ArrayList<>();

    @NotNull
    @Column(name = "reduction_key")
    private String reductionKey;

    @NotNull
    @Column(name = "clear_key")
    private String clearKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AlertType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "managed_object_type")
    private ManagedObjectType managedObjectType;
}

