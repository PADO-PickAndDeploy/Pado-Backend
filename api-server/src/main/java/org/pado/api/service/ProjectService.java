package org.pado.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.ProjectCreateRequest;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.ProjectCreateResponse;
import org.pado.api.dto.response.ProjectDetailResponse;
import org.pado.api.dto.response.ProjectListResponse;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.project.ProjectRunningStatus;
import org.pado.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.pado.api.domain.component.ComponentRepository;
import org.pado.api.domain.connection.ConnectionRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ComponentRepository componentRepository;
    private final ConnectionRepository connectionRepository;

    private static void validateComponentCreation(Project project) {
        if (project.getDeploymentStatus() != ProjectDeploymentStatus.DRAFT &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.TERMINATED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.DEPLOYED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트가 실행 중이거나 배포 상태입니다. 설정을 변경할 수 없습니다.");
        }
    }

    @Transactional
    public ProjectCreateResponse createProject(ProjectCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .thumbnail(null)
                .deploymentStatus(ProjectDeploymentStatus.DRAFT)
                .runningStatus(ProjectRunningStatus.DRAFT)
                .build();
        try {
            if (projectRepository.existsByUserIdAndName(user.getId(), project.getName())) {
            throw new CustomException(ErrorCode.PROJECT_ALREADY_EXISTS, 
                    "프로젝트 이름 '" + project.getName() + "'은 이미 존재합니다.");
            }
            projectRepository.save(project);
        } catch (CustomException e) {
            log.warn("CustomException occurred: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating project for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 생성 중 오류가 발생했습니다.");
        }

        return new ProjectCreateResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getDeploymentStatus(),
                project.getRunningStatus(),
                project.getThumbnail(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProjectListResponse getProjects(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<Project> projects;

        try {
            projects = projectRepository.findByUserId(user.getId());
        } catch (Exception e) {
            log.error("Error occurred while fetching projects for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 목록을 조회하는 중 오류가 발생했습니다.");
        }

        List<ProjectListResponse.ProjectInfo> projectInfos = projects.stream()
                .map(project -> new ProjectListResponse.ProjectInfo(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getDeploymentStatus(),
                        project.getRunningStatus(),
                        project.getThumbnail(),
                        project.getCreatedAt(),
                        project.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return new ProjectListResponse(projectInfos);
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long id, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Project project;
        List<ProjectDetailResponse.ComponentInfo> components;

        try {
            project = projectRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
        } catch (CustomException e) {
            log.warn("Project not found for user: {}, project ID: {}", user.getId(), id);
            throw e; // Re-throwing the custom exception
        } catch (Exception e) {
            log.error("Error occurred while fetching project detail for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 상세 정보를 조회하는 중 오류가 발생했습니다.");
        }

        try {
            components = componentRepository.findByProjectIdAndParent(project.getId(), null)
                    .stream()
                    .map(component -> new ProjectDetailResponse.ComponentInfo(
                            component.getId(),
                            component.getVersion(),
                            component.getName(),
                            component.getType(),
                            component.getSubtype(),
                            component.getThumbnail(),
                            component.getDeploymentStatus(),
                            component.getRunningStatus(),
                            component.getDeployStartTime(),
                            component.getDeployEndTime(),
                            component.getChildren().stream()
                                    .map(child -> new ProjectDetailResponse.ComponentInfo(
                                            child.getId(),
                                            child.getVersion(),
                                            child.getName(),
                                            child.getType(),
                                            child.getSubtype(),
                                            child.getThumbnail(),
                                            child.getDeploymentStatus(),
                                            child.getRunningStatus(),
                                            child.getDeployStartTime(),
                                            child.getDeployEndTime(),
                                            null,
                                            connectionRepository.findByFromComponent(child).stream()
                                                    .map(conn -> new ProjectDetailResponse.ConnectionInfo(
                                                            conn.getId(),
                                                            conn.getFromComponent().getId(),
                                                            conn.getToComponent().getId(),
                                                            conn.getFromPort(),
                                                            conn.getToPort()
                                                    )).collect(Collectors.toList())
                                    )).collect(Collectors.toList()),
                            connectionRepository.findByFromComponent(component).stream()
                                    .map(conn -> new ProjectDetailResponse.ConnectionInfo(
                                            conn.getId(),
                                            conn.getFromComponent().getId(),
                                            conn.getToComponent().getId(),
                                            conn.getFromPort(),
                                            conn.getToPort()
                                    )).collect(Collectors.toList())
                    ))
                    .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error occurred while fetching components for project: {}", project.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 컴포넌트를 조회하는 중 오류가 발생했습니다.");
        }

        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getThumbnail(),
                project.getDeploymentStatus(),
                project.getRunningStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                components
        );
    }

    @Transactional
    public DefaultResponse deleteProject(Long id, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Project project;

        try {
            project = projectRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            projectRepository.delete(project);
        } catch (CustomException e) {
            log.warn("Project not found for user: {}, project ID: {}", user.getId(), id);
            throw e; // Re-throwing the custom exception
        } catch (Exception e) {
            log.error("Error occurred while deleting project for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 삭제 중 오류가 발생했습니다.");
        }
        return new DefaultResponse("프로젝트가 성공적으로 삭제되었습니다.");
    }
}
