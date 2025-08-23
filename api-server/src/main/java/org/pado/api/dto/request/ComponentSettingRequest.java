package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "컴포넌트 설정 요청 DTO")
public class ComponentSettingRequest {
    private String settingJson;
}
