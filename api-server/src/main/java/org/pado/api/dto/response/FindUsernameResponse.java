package org.pado.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FindUsernameResponse {
    @Schema(description = "마스킹된 사용자명 (계정이 없으면 null)", 
            example = "us***23", nullable = true)
    private String name;
}
