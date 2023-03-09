
# TODO

## Pagination support

Implement proper request filtering and pagination support.

See `org.opennms.horizon.alarmservice.grpc.AlarmGrpcService#listAlarms`

## Modeling and API contract for the "alarm definitions

See `org.opennms.horizon.alarmservice.db.repository.AlarmDefinitionRepository` and `org.opennms.horizon.alarmservice.grpc.AlarmConfigurationGrpcService`.

## Scalable implementation of the "alarm engine"

See `org.opennms.horizon.alarmservice.service.AlarmEngine`
