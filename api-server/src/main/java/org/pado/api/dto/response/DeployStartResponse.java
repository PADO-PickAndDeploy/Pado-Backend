package org.pado.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Deployment Start Response")
public class DeployStartResponse {
    @Schema(description = "Deployment request time")
    private LocalDateTime requestTime;

    @Schema(description = "Deployment start message")
    private String message;
}
