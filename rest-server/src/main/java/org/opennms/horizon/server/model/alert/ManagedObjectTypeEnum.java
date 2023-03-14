package org.opennms.horizon.server.model.alert;

import lombok.Getter;

@Getter
public enum ManagedObjectTypeEnum {
    UNDEFINED,
    ANY,
    NODE,
    SNMP_INTERFACE,
    SNMP_INTERFACE_LINK,
    UNRECOGNIZED
}
