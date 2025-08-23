package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "컴포넌트 설정 요청 DTO")
public class ComponentSettingRequest {
    @NotNull
    @Positive   
    @Schema(description = "포트 번호", example = "8080")
    private Long port;

    @NotBlank(message = "설정 JSON은 필수입니다.")
    @Schema(description = "설정 JSON", example = "{\"key\":\"value\"}")
    private String settingJson;
}
