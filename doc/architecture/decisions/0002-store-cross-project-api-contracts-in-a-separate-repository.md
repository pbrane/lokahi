# 2. Store cross-project API contracts in a separate repository

Date: 2023-03-20

## Status

Proposed

## Context

OpenNMS maintains different systems that need to communicate with each other. Horizon Stream is one of these systems. It acts as a client of another OpenNMS system, which is maintained as a different project by a different team.

We need a way to share the API contracts between different systems and ease cross-team coordination when changes to them occur. These contracts are defined as gRPC proto files. In order to make use of them, we need to generate code for both the server and client. This code can be generated ahead of time and provided as a library, or at build-time when using the proto files directly.

No team/system should be considered the "owner" of a contract. They would be able to make changes to it without considering the needs of other systems, so the risk of introducing breaking changes is high. Any changes would need to be manually communicated and coordinated with other teams. This would be an error-prone and time-consuming process, and the number of necessary communication paths would be significantly higher.

Contracts should be versioned. This would allow for well-controlled releases, without needing to update dependent components in lock-step with the latest changes. Any changes to the API contracts should be reviewed and approved by the owners of the components that depend on them, e.g. team members for each of the clients and servers of a given contract. A git repository for these contracts would function well as a common ground for different teams to coordinate these changes.

The best solution for distributing these API contracts is to publish libraries that contain the proto files, along with generated client/server code. Using a mature dependency distribution system like Maven would save us a lot of time and headache. The tradeoff is this needs to be done for each of the programming languages we support.

We need to publish libraries for Java to a publicly accessible Maven repository. OpenNMS has access to Maven repositories provided by Cloudsmith and GitHub Container Registry. Libraries for other programming languages can be done in a similar fashion, e.g. generating a Golang module and publishing it to GHCR.

We should automate this process, and a CI system like GitHub Actions would be a good fit. It can validate the proto files, generate the code, package it into an artifact, and publish it to the repository without any human intervention.

## Decision

We will store cross-project API contracts in a new git repository. Changes to these API contracts will be done with pull requests. All proto files that are shared between projects will be moved to this new repository.

In order for a PR to be merged, a team member from each of its dependent projects will be required to approve it. A GitHub CODEOWNERS file can be used to enforce this by specifying each of the teams as an owner of the proto file.

API contracts will be published as versioned libraries that contain generated code. These libraries will be generated for each of the proto files in each of programming languages we support, and published to a publicly accessible artifact registry.

We will use a CI system to compile and publish the libraries. They will be versioned using an automated release process.

Downstream projects will depend on a specific version of these libraries using the appropriate dependency management tooling for the project. They will not depend on them with dynamic versions like Maven's `x.x.x-SNAPSHOT` feature.

## Consequences

- Moving the contracts to their own repository will ease coordination between teams.
- A release process for proto files means they are free to change without immediately breaking dependent systems.
- We will need to build and maintain pipelines to create and publish libraries.
- We will need to repeat the above step for any new programming languages we want to support.
- We will need to decide on and implement a release process for these libraries.
- Breaking changes to proto files can be caught much easier, as any changes need to be reviewed by the people who should know the impacts.
- Removing the proto files from the Horizon Stream repository means there will be fewer modules in `shared-lib`.
