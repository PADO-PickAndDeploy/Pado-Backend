package org.pado.api.dto.response;

import org.pado.api.domain.connection.ConnectionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ConnectionCreateResponse {
    @Schema(description = "연결 ID", example = "1")
    private Long connectionId;

    @Schema(description = "연결 유형", example = "TCP")
    private ConnectionType connectionType;

    @Schema(description = "대상 컴포넌트 ID", example = "2")
    private Long targetComponentId;

    @Schema(description = "출발 컴포넌트 ID", example = "1")
    private Long sourceComponentId;

    @Schema(description = "출발 포트", example = "8080")
    private Long fromPort;
    
    @Schema(description = "도착 포트", example = "8081")
    private Long toPort;
}
