package org.pado.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "기본 응답 DTO")
public class DefaultResponse {
    @Schema(description = "응답 메시지")
    private String message;
}
