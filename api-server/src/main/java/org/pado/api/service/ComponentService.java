package org.pado.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.domain.component.Component;
import org.pado.api.domain.component.ComponentList;
import org.pado.api.domain.component.ComponentListRepository;
import org.pado.api.domain.component.ComponentRepository;
import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.user.User;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {
    private final ComponentListRepository componentListRepository;
    private final ComponentRepository componentRepository;
    private final ProjectRepository projectRepository;

    public ComponentListResponse getComponentList() {
        List<ComponentListResponse.ComponentListInfo> components;
        try {
            components = componentListRepository.findAll().stream()
                    .map(component -> new ComponentListResponse.ComponentListInfo(
                            component.getId(),
                            component.getName(),
                            component.getDescription(),
                            component.getThumbnail(),
                            component.getType(),
                            component.getSubtype())
            ).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while fetching component list", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 목록 조회 중 오류가 발생했습니다.");
        }

        return new ComponentListResponse(components);
    }

    public ComponentCreateResponse createComponent(Long projectId, ComponentCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        // Try Catch 를 통한 에러 처리 필요
        ComponentList resourceComponent = componentListRepository.findByTypeAndSubtype(ComponentType.RESOURCE, ComponentSubType.valueOf(request.getResourceType()))
                .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "리소스 컴포넌트를 찾을 수 없습니다."));
        ComponentList serviceComponent = componentListRepository.findByTypeAndSubtype(ComponentType.SERVICE, ComponentSubType.valueOf(request.getServiceType()))
                .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "서비스 컴포넌트를 찾을 수 없습니다."));

        Component parentComponent;
        Component component;

        // 컴포넌트 종류 별로 에러 처리 필요. (리소스 타입에 리소스 SubType 사용 불가능, -> Enum 나눌 필요도 있을 듯)

        if (request.getParentId() != null) {
            parentComponent = componentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "부모 컴포넌트를 찾을 수 없습니다."));
        } else {
            try {
                parentComponent = Component.builder()
                        .project(project)
                        .name(resourceComponent.getName())
                        .type(resourceComponent.getType())
                        .subtype(resourceComponent.getSubtype())
                        .thumbnail(resourceComponent.getThumbnail())
                        .deployStartTime(null)
                        .deployEndTime(null)
                        .build();
                componentRepository.save(parentComponent);
            } catch (Exception e) {
                log.error("Error occurred while creating parent component", e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "부모 컴포넌트 생성 중 오류가 발생했습니다.");
            }
        }
        try {
            component = Component.builder()
                    .project(project)
                    .parent(parentComponent)
                    .name(serviceComponent.getName())
                    .type(serviceComponent.getType())
                    .subtype(serviceComponent.getSubtype())
                    .thumbnail(serviceComponent.getThumbnail())
                    .deployStartTime(null)
                    .deployEndTime(null)
                    .build();

            componentRepository.save(component);
        } catch (Exception e) {
            log.error("Error occurred while creating component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 생성 중 오류가 발생했습니다.");
        }

        return new ComponentCreateResponse(
                new ComponentCreateResponse.ComponentCreateInfo(
                        parentComponent.getId(),
                        parentComponent.getVersion(),
                        parentComponent.getType(),
                        parentComponent.getSubtype()
                ),
                new ComponentCreateResponse.ComponentCreateInfo(
                        component.getId(),
                        component.getVersion(),
                        component.getType(),
                        component.getSubtype()
                )
        );
    }
}
