package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComponentCreateRequest {
    @NotBlank(message = "리소스 서브타입은 필수입니다.")
    @Schema(description = "컴포넌트 타입", example = "S3")
    private String resourceType;

    @NotBlank(message = "서비스 서브타입은 필수입니다.")
    @Schema(description = "컴포넌트 서브 타입", example = "Spring")
    private String serviceType;

    @Schema(description = "부모 컴포넌트 ID", example = "1")
    private Long parentId;
}
