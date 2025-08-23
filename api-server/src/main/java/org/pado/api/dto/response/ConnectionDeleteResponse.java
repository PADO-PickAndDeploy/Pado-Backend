package org.pado.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "연결 삭제 응답")
public class ConnectionDeleteResponse {

    @Schema(description = "삭제 성공 메시지", example = "연결이 성공적으로 삭제되었습니다.")
    private String message;
    
}
