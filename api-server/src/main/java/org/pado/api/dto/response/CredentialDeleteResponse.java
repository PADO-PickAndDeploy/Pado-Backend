package org.pado.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDeleteResponse {

    @Schema(description = "삭제 처리 메시지", example = "크리덴셜이 성공적으로 삭제되었습니다.")
    private String message;
}
