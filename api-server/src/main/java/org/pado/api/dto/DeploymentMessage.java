package org.pado.api.dto;

import org.pado.api.domain.deployment.Deployment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentMessage {
    private String operation;
    private String wrappedToken;
    private String deploymentId;
    private Deployment deployment;
}
