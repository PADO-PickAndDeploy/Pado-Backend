package org.pado.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DeployStopResponse {
    @Schema(description = "Deployment request time")
    private LocalDateTime requestTime;

    @Schema(description = "Deployment stop message")
    private String message;

    @Schema(description = "Deployment ID")
    private String deploymentId;
}
