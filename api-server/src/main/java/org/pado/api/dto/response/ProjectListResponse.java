package org.pado.api.dto.response;

import org.pado.api.domain.common.Status;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "프로젝트 목록 응답 DTO")
public class ProjectListResponse {
    private List<ProjectInfo> projects;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        @Schema(description = "프로젝트 ID", example = "1")
        private Long id;

        @Schema(description = "프로젝트 이름", example = "PADO Project")
        private String name;

        @Schema(description = "프로젝트 설명", example = "This is a sample project description.")
        private String description;

        @Schema(description = "프로젝트 상태", example = "DRAFT")
        private Status status;

        @Schema(description = "프로젝트 썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "생성 시간", example = "2023-10-01T12:00:00Z")
        private LocalDateTime createdAt;

        @Schema(description = "수정 시간", example = "2023-10-01T12:00:00Z")
        private LocalDateTime updatedAt;
    }
}