package org.pado.api.domain.component;

public enum ComponentDeploymentStatus {
    DRAFT,
    QUEUED,
    DEPLOYING,
    DEPLOYED,
    STOP_REQUESTED,
    TERMINATING,
    TERMINATED,
    FAILED,
    CANCELLED
}
