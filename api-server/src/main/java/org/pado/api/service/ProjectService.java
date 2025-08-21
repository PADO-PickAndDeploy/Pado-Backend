package org.pado.api.service;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.ProjectCreateRequest;
import org.pado.api.dto.response.ProjectResponse;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.pado.api.domain.common.Status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectResponse createProject(ProjectCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .thumbnail(null)
                .build();
        if (projectRepository.existsByUserIdAndName(user.getId(), project.getName())) {
            throw new CustomException(ErrorCode.PROJECT_ALREADY_EXISTS, 
                "프로젝트 이름 '" + project.getName() + "'은 이미 존재합니다.");
        }
        projectRepository.save(project);
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                Status.ACTIVE,
                project.getThumbnail(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
