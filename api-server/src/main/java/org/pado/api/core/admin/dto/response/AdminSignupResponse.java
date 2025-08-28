package org.pado.api.core.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSignupResponse {
     @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "응답 메시지", example = "회원가입 성공")
    private String message;
}
