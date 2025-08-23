package org.pado.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.domain.component.Component;
import org.pado.api.domain.component.ComponentDefaultSetting;
import org.pado.api.domain.component.ComponentDefaultSettingRepository;
import org.pado.api.domain.component.ComponentList;
import org.pado.api.domain.component.ComponentListRepository;
import org.pado.api.domain.component.ComponentRepository;
import org.pado.api.domain.component.ComponentSetting;
import org.pado.api.domain.component.ComponentSettingRepository;
import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.user.User;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {
    private final ComponentListRepository componentListRepository;
    private final ComponentRepository componentRepository;
    private final ProjectRepository projectRepository;
    private final ComponentDefaultSettingRepository componentDefaultSettingRepository;
    private final ComponentSettingRepository componentSettingRepository;

    public ComponentListResponse getComponentList() {
        List<ComponentListResponse.ComponentListInfo> components;
        try {
            components = componentListRepository.findAll().stream()
                    .map(component -> new ComponentListResponse.ComponentListInfo(
                            component.getId(),
                            component.getName(),
                            component.getDescription(),
                            component.getResourceThumbnail(),
                            component.getServiceThumbnail(),
                            component.getResourceType(),
                            component.getServiceType())
            ).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while fetching component list", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 목록 조회 중 오류가 발생했습니다.");
        }

        return new ComponentListResponse(components);
    }
    
    @Transactional
    public ComponentCreateResponse createComponent(Long projectId, ComponentCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        // Try Catch 를 통한 에러 처리 필요
        ComponentList selectedComponent;
        try {
            selectedComponent = componentListRepository.findByResourceTypeAndServiceType(
                    ComponentSubType.valueOf(request.getResourceType()),
                    ComponentSubType.valueOf(request.getServiceType()))
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "리소스 컴포넌트를 찾을 수 없습니다."));
        } catch (IllegalArgumentException e) {
            log.error("Invalid ComponentSubType: resourceType={}, serviceType={}", request.getResourceType(), request.getServiceType());
            throw new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "유효하지 않은 컴포넌트 유형입니다.");
        } catch (Exception e) {
            log.error("Error occurred while fetching component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 조회 중 오류가 발생했습니다.");
        }

        Component parentComponent;
        Component component;

        if (request.getParentId() != null) {
            parentComponent = componentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "부모 컴포넌트를 찾을 수 없습니다."));
            if (parentComponent.getSubtype() != selectedComponent.getResourceType()) {
                log.error("Parent component subtype mismatch: parent={}, child={}", parentComponent.getSubtype(), selectedComponent.getResourceType());
                throw new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "부모 컴포넌트의 유형이 일치하지 않습니다.");
            }
        } else {
            try {
                parentComponent = Component.builder()
                        .project(project)
                        .name(selectedComponent.getResourceType().toString())
                        .type(ComponentType.RESOURCE)
                        .subtype(selectedComponent.getResourceType())
                        .thumbnail(selectedComponent.getResourceThumbnail())
                        .version(1L)
                        .deployStartTime(null)
                        .deployEndTime(null)
                        .build();
                componentRepository.save(parentComponent);
            } catch (Exception e) {
                log.error("Error occurred while creating parent component", e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "부모 컴포넌트 생성 중 오류가 발생했습니다.");
            }

            try {
                ComponentSetting componentSetting = ComponentSetting.builder()
                        .componentId(parentComponent.getId())
                        .version(parentComponent.getId())
                        .type(parentComponent.getSubtype())
                        .value(componentDefaultSettingRepository.findByType(parentComponent.getSubtype())
                                .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트 기본 설정을 찾을 수 없습니다."))
                                .getValue())
                        .build();
                componentSettingRepository.save(componentSetting);
            } catch (Exception e) {
                log.error("Error occurred while creating component setting", e);
                componentRepository.delete(parentComponent);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 설정 생성 중 오류가 발생했습니다.");
            }
        }
        try {
            component = Component.builder()
                    .project(project)
                    .parent(parentComponent)
                    .name(selectedComponent.getServiceType().toString())
                    .type(ComponentType.SERVICE)
                    .subtype(selectedComponent.getServiceType())
                    .thumbnail(selectedComponent.getServiceThumbnail())
                    .version(1L)
                    .deployStartTime(null)
                    .deployEndTime(null)
                    .build();

            componentRepository.save(component);
        } catch (Exception e) {
            log.error("Error occurred while creating component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 생성 중 오류가 발생했습니다.");
        }

        try {
            ComponentSetting componentSetting = ComponentSetting.builder()
                    .componentId(component.getId())
                    .version(component.getVersion())
                    .type(component.getSubtype())
                    .value(componentDefaultSettingRepository.findByType(component.getSubtype())
                            .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트 기본 설정을 찾을 수 없습니다."))
                            .getValue())
                    .build();
            componentSettingRepository.save(componentSetting);
        } catch (Exception e) {
            log.error("Error occurred while creating component setting", e);
            componentRepository.delete(component);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 설정 생성 중 오류가 발생했습니다.");
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
