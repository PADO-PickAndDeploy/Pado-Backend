package org.pado.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.ProjectCreateRequest;
import org.pado.api.dto.response.ProjectCreateResponse;
import org.pado.api.dto.response.ProjectListResponse;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.pado.api.domain.common.Status;


import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectCreateResponse createProject(ProjectCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .thumbnail(null)
                .build();
        try {
            if (projectRepository.existsByUserIdAndName(user.getId(), project.getName())) {
            throw new CustomException(ErrorCode.PROJECT_ALREADY_EXISTS, 
                    "프로젝트 이름 '" + project.getName() + "'은 이미 존재합니다.");
            }
            projectRepository.save(project);
        } catch (Exception e) {
            log.error("Error occurred while creating project for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 생성 중 오류가 발생했습니다.");
        }

        return new ProjectCreateResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                Status.ACTIVE,
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
                        Status.START,
                        project.getThumbnail(),
                        project.getCreatedAt(),
                        project.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return new ProjectListResponse(projectInfos);
    }
}
