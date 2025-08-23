package org.pado.api.dto.response;

import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.component.ComponentSubType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ComponentCreateResponse {
    @Schema(description = "부모 컴포넌트")
    ComponentCreateInfo parentComponent;
    
    @Schema(description = "자식 컴포넌트")
    ComponentCreateInfo childComponent;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class ComponentCreateInfo {
        @Schema(description = "컴포넌트 ID", example = "1")
        private Long id;

        @Schema(description = "컴포넌트 버전", example = "1")
        private Long version;

        @Schema(description = "컴포넌트 타입", example = "RESOURCE")
        private ComponentType type;

        @Schema(description = "컴포넌트 서브타입", example = "S3")
        private ComponentSubType subtype;
    }
}
