package org.pado.api.domain.project;

public enum ProjectDeploymentStatus {
    DRAFT,
    QUEUED,
    DEPLOYING,
    DEPLOYED,
    STOP_REQUESTED,
    TERMINATING,
    TERMINATED,
    FAILED
}
