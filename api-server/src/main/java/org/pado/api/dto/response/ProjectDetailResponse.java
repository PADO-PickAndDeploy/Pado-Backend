package org.pado.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRunningStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import org.pado.api.domain.component.ComponentDeploymentStatus;
import org.pado.api.domain.component.ComponentRunningStatus;
import org.pado.api.domain.component.ComponentSubType;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProjectDetailResponse {
    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    @Schema(description = "프로젝트 이름", example = "PADO Project")
    private String name;

    @Schema(description = "프로젝트 설명", example = "This is a sample project description.")
    private String description;

    @Schema(description = "프로젝트 썸네일 URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnail;

    @Schema(description = "프로젝트 상태", example = "ACTIVE")
    private ProjectDeploymentStatus deploymentStatus;

    @Schema(description = "프로젝트 실행 상태", example = "DRAFT")
    private ProjectRunningStatus runningStatus;

    @Schema(description = "생성 시간", example = "2023-10-01T12:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2023-10-01T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "프로젝트에 속한 컴포넌트 목록")
    private List<ComponentInfo> components;


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionInfo {
        @Schema(description = "연결 ID", example = "1")
        Long id;
        
        @Schema(description = "연결 소스 컴포넌트 ID", example = "2")
        Long fromComponentId;

        @Schema(description = "연결 대상 컴포넌트 ID", example = "2")
        Long toComponentId;

        @Schema(description = "연결 소스 포트", example = "80")
        Long fromPort;

        @Schema(description = "연결 대상 포트", example = "8080")
        Long toPort;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentInfo {
        @Schema(description = "컴포넌트 ID", example = "1")
        private Long id;

        @Schema(description = "컴포넌트 이름", example = "MyComponent")
        private String name;

        @Schema(description = "컴포넌트 타입", example = "RESOURCE")
        private ComponentType type;

        @Schema(description = "컴포넌트 서브 타입", example = "S3")
        private ComponentSubType subtype;

        @Schema(description = "컴포넌트 썸네일 URL", example = "https://example.com/component-thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "컴포넌트 상태", example = "ACTIVE")
        private ComponentDeploymentStatus deploymentStatus;

        @Schema(description = "컴포넌트 실행 상태", example = "DRAFT")
        private ComponentRunningStatus runningStatus;

        @Schema(description = "배포 시작 시간", example = "2023-10-01T12:00:00Z")
        private LocalDateTime deployStartTime;

        @Schema(description = "배포 종료 시간", example = "2023-10-01T12:00:00Z")
        private LocalDateTime deployEndTime;

        @Schema(description = "자식 컴포넌트 리스트")
        private List<ComponentInfo> children;

        @Schema(description = "연결 정보 리스트")
        private List<ConnectionInfo> connections;
    }
}
