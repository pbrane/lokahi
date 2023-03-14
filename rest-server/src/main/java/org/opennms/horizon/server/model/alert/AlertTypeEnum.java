package org.opennms.horizon.server.model.alert;

import lombok.Getter;

@Getter
public enum AlertTypeEnum {
    ALARM_TYPE_UNDEFINED,
    PROBLEM_WITH_CLEAR,
    CLEAR,
    PROBLEM_WITHOUT_CLEAR,
    UNRECOGNIZED
}
