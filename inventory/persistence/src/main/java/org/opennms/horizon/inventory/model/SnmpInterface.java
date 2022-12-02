package org.opennms.horizon.inventory.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.basic.Inet;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLInetType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@TypeDef(
    name = "ipv4",
    typeClass = PostgreSQLInetType.class,
    defaultForType = Inet.class
)
public class SnmpInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", referencedColumnName = "id")
    private Node node;

    @Column(name = "node_id", insertable = false, updatable = false)
    private long nodeId;

    @Column(name = "ip_address", columnDefinition = "inet")
    private Inet ipAddress;

    @NotNull
    @Column(name = "if_index")
    private int ifIndex;

    @Column(name = "if_descr")
    private String ifDescr;
}
