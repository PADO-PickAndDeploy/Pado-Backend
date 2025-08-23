package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequest {
    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    @Schema(description = "프로젝트 이름", example = "PADO Project")
    private String name;

    @NotBlank(message = "프로젝트 설명은 필수입니다.")
    @Schema(description = "프로젝트 설명", example = "This is a sample project description.")
    private String description;
}
