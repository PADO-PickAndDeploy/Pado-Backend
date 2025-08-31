package org.pado.api.dto.response;

import java.util.List;

import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ComponentListResponse {
    private List<ComponentListInfo> components;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentListInfo {
        @Schema(description = "컴포넌트 ID", example = "1")
        private Long id;

        @Schema(description = "컴포넌트 이름", example = "MyComponent")
        private String name;

        @Schema(description = "컴포넌트 버전", example = "1")
        private String description;

        @Schema(description = "컴포넌트 썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String resourceThumbnail;

        @Schema(description = "컴포넌트 서비스 썸네일 URL", example = "https://example.com/service_thumbnail.jpg")
        private String serviceThumbnail;

        @Schema(description = "컴포넌트 리소스 타입", example = "S3")
        private ComponentSubType resourceType;

        @Schema(description = "컴포넌트 서비스 타입", example = "REACT")
        private ComponentSubType serviceType;
    }
}
