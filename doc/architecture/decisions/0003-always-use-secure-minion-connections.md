# 3. Always Use Secure Minion Connections

Date: 2023-03-31

## Status

Proposed

## Context

Minions will be deployed to monitor remote networks in various locations. The core system may need to accept minion connections from both internally connected networks as well as those traversing the external internet.

## Decision

Minions will only be able to connect to Horizon Stream when they are using a proper TLS client certificate.

## Consequences

All minion connections into Horizon Stream will be authenticated through the use of certificates. Any connection attempts to the system ingress that do not use the expected client certificates will be denied. This will provide protection against unauthorized connections, especially in cases where the minion is remote and could be connecting back to Horizon Stream over external or public networks.

Developers will be forced to use client certificates with their minion deployments. This will result in some extra steps when deploying the minion, but will also ensure the secure code paths are well tested.

The default Horizon Stream deployment will require a test certificate authority as part of the default install so that developers do not need to deal with CAs when developing or testing.