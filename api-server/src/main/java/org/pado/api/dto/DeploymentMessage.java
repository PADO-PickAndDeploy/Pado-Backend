package org.pado.api.dto;

import org.pado.api.domain.deployment.Deployment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentMessage {
    private String wrappedToken;
    private Deployment deployment;
}
