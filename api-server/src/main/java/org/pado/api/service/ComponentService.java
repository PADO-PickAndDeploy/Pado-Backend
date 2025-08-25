package org.pado.api.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.domain.component.Component;
import org.pado.api.domain.component.ComponentDefaultSetting;
import org.pado.api.domain.component.ComponentDefaultSettingRepository;
import org.pado.api.domain.component.ComponentDeploymentStatus;
import org.pado.api.domain.component.ComponentList;
import org.pado.api.domain.component.ComponentListRepository;
import org.pado.api.domain.component.ComponentRepository;
import org.pado.api.domain.component.ComponentRunningStatus;
import org.pado.api.domain.component.ComponentSetting;
import org.pado.api.domain.component.ComponentSettingRepository;
import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.connection.Connection;
import org.pado.api.domain.connection.ConnectionRepository;
import org.pado.api.domain.connection.ConnectionType;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.user.User;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.request.ComponentSettingRequest;
import org.pado.api.dto.request.ConnectionCreateRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentDeleteResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.pado.api.dto.response.ComponentSettingResponse;
import org.pado.api.dto.response.ConnectionCreateResponse;
import org.pado.api.dto.response.ConnectionDeleteResponse;
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
    private final ConnectionRepository connectionRepository;

    private static String generateUniqueName(String name) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8).toLowerCase();
        return name + "-" + randomSuffix;
    }

    private static void validateComponentCreation(Project project) {
        if (project.getDeploymentStatus() != ProjectDeploymentStatus.DRAFT &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.TERMINATED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.DEPLOYED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트가 실행 중이거나 배포 상태입니다. 설정을 변경할 수 없습니다.");
        }
    }

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

        componentDefaultSettingRepository.findAll().forEach(elem -> {
            log.info("ComponentDefaultSetting: id={}, type={}, value={}", elem.getId(), elem.getType(), elem.getValue());
        });
        return new ComponentListResponse(components);
    }
    
    @Transactional
    public ComponentCreateResponse createComponent(Long projectId, ComponentCreateRequest request, CustomUserDetails userDetails) {
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 삭제 불가)
        User user = userDetails.getUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
        validateComponentCreation(project);

        // Try Catch 를 통한 에러 처리 필요
        ComponentList selectedComponent;
        Component parentComponent;
        Component component;
        try {
            selectedComponent = componentListRepository.findByResourceTypeAndServiceType(
                    ComponentSubType.valueOf(request.getResourceType()),
                    ComponentSubType.valueOf(request.getServiceType()))
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "리소스 컴포넌트를 찾을 수 없습니다."));
        } catch (CustomException e) {
            log.error("CustomException occurred: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid ComponentSubType: resourceType={}, serviceType={}", request.getResourceType(), request.getServiceType());
            throw new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "유효하지 않은 컴포넌트 유형입니다.");
        } catch (Exception e) {
            log.error("Error occurred while fetching component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 조회 중 오류가 발생했습니다.");
        }

        if (request.getParentId() != null) {
            parentComponent = componentRepository.findByIdAndProjectId(request.getParentId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "부모 컴포넌트를 찾을 수 없습니다."));
            if (parentComponent.getSubtype() != selectedComponent.getResourceType()) {
                log.error("Parent component subtype mismatch: parent={}, child={}", parentComponent.getSubtype(), selectedComponent.getResourceType());
                throw new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "부모 컴포넌트의 유형이 일치하지 않습니다.");
            }
        } else {
            try {
                parentComponent = Component.builder()
                        .project(project)
                        .name(generateUniqueName(selectedComponent.getResourceType().toString()))
                        .type(ComponentType.RESOURCE)
                        .subtype(selectedComponent.getResourceType())
                        .thumbnail(selectedComponent.getResourceThumbnail())
                        .version(1L)
                        .deploymentStatus(ComponentDeploymentStatus.DRAFT)
                        .runningStatus(ComponentRunningStatus.DRAFT)
                        .deployStartTime(null)
                        .deployEndTime(null)
                        .build();
                componentRepository.save(parentComponent);
            } catch (Exception e) {
                log.error("Error occurred while creating parent component", e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "부모 컴포넌트 생성 중 오류가 발생했습니다.");
            }

            try {
                ComponentDefaultSetting defaultSetting = componentDefaultSettingRepository.findByType(parentComponent.getSubtype())
                        .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트 기본 설정을 찾을 수 없습니다."));
                ComponentSetting componentSetting = ComponentSetting.builder()
                        .componentId(parentComponent.getId())
                        .version(parentComponent.getId())
                        .type(parentComponent.getSubtype())
                        .port(defaultSetting.getDefaultPort())
                        .value(defaultSetting.getValue())
                        .build();
                componentSettingRepository.save(componentSetting);
            } catch (CustomException e) {
                log.error("Custom error occurred while creating component setting", e);
                throw e;
            } catch (Exception e) {
                log.error("Error occurred while creating component setting", e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 설정 생성 중 오류가 발생했습니다.");
            }
        }
        try {
            component = Component.builder()
                    .project(project)
                    .parent(parentComponent)
                    .name(generateUniqueName(selectedComponent.getServiceType().toString()))
                    .type(ComponentType.SERVICE)
                    .subtype(selectedComponent.getServiceType())
                    .thumbnail(selectedComponent.getServiceThumbnail())
                    .version(1L)
                    .deploymentStatus(ComponentDeploymentStatus.DRAFT)
                    .runningStatus(ComponentRunningStatus.DRAFT)
                    .deployStartTime(null)
                    .deployEndTime(null)
                    .build();

            componentRepository.save(component);
        } catch (Exception e) {
            log.error("Error occurred while creating component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 생성 중 오류가 발생했습니다.");
        }

        try {
            ComponentDefaultSetting defaultSetting = componentDefaultSettingRepository.findByType(component.getSubtype())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트 기본 설정을 찾을 수 없습니다."));
            ComponentSetting componentSetting = ComponentSetting.builder()
                    .componentId(component.getId())
                    .version(component.getVersion())
                    .type(component.getSubtype())
                    .port(defaultSetting.getDefaultPort())
                    .value(defaultSetting.getValue())
                    .build();
            componentSettingRepository.save(componentSetting);
        } catch (CustomException e) {
            log.error("Custom error occurred while creating component setting", e);
            throw e;
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
                        parentComponent.getSubtype(),
                        parentComponent.getName(),
                        parentComponent.getThumbnail(),
                        parentComponent.getDeploymentStatus(),
                        parentComponent.getRunningStatus()
                ),
                new ComponentCreateResponse.ComponentCreateInfo(
                        component.getId(),
                        component.getVersion(),
                        component.getType(),
                        component.getSubtype(),
                        component.getName(),
                        component.getThumbnail(),
                        component.getDeploymentStatus(),
                        component.getRunningStatus()
                )
        );
    }

    @Transactional
    public ComponentSettingResponse setComponentSetting(Long projectId, Long componentId, ComponentSettingRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 수정 불가)
        try {
            Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            Component component = componentRepository.findByIdAndProjectUserId(componentId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트를 찾을 수 없습니다."));
            // 컴포넌트 연결 업데이트
            component.getFromConnections().forEach(connection -> {
                connection.setFromPort(request.getPort());
                connectionRepository.save(connection);
            });
            component.getToConnections().forEach(connection -> {
                connection.setToPort(request.getPort());
                connectionRepository.save(connection);
            });
            ComponentSetting componentSetting = componentSettingRepository.findFirstByComponentIdOrderByVersionDesc(component.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트 설정을 찾을 수 없습니다."));
            componentSetting.setPort(request.getPort());
            componentSetting.setValue(request.getSettingJson());
            componentSettingRepository.save(componentSetting);
        } catch (Exception e) {
            log.error("Error occurred while updating component setting", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 설정 변경 중 오류가 발생했습니다.");
        }
        return new ComponentSettingResponse("컴포넌트 설정이 성공적으로 변경되었습니다.");
    }

    @Transactional
    public ComponentDeleteResponse deleteComponent(Long projectId, Long componentId, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 삭제 불가)
        try {
            Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            componentRepository.findByIdAndProjectUserId(componentId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트를 찾을 수 없습니다."));
            List<ComponentSetting> componentSettings = componentSettingRepository.findByComponentId(componentId);
            componentSettings.forEach(componentSetting -> {
                componentSettingRepository.delete(componentSetting);
            });
        } catch (CustomException e) {
            log.error("Error occurred while deleting component settings", e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting component settings", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 설정 삭제 중 오류가 발생했습니다.");
        }
        try {
            Component component = componentRepository.findByIdAndProjectUserId(componentId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트를 찾을 수 없습니다."));
            componentRepository.delete(component);
        } catch (Exception e) {
            log.error("Error occurred while deleting component", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 삭제 중 오류가 발생했습니다.");
        }
        return new ComponentDeleteResponse("컴포넌트가 성공적으로 삭제되었습니다.");
    }

    @Transactional
    public ConnectionCreateResponse createConnection(Long projectId, Long sourceComponentId, ConnectionCreateRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        try {
            Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            Component sourceComponent = componentRepository.findByIdAndProjectUserIdAndProjectId(sourceComponentId, user.getId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "소스 컴포넌트를 찾을 수 없습니다."));
            Component targetComponent = componentRepository.findByIdAndProjectUserIdAndProjectId(request.getTargetComponentId(), user.getId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "타겟 컴포넌트를 찾을 수 없습니다."));

            // Source Component, Target Component에 대해서 연결이 가능한지 여부 확인 필요 (예: Service -> Service, Resource 불가능, React -> Spring, Spring -> MySQL)
            ComponentSetting sourceSetting = componentSettingRepository.findFirstByComponentIdOrderByVersionDesc(sourceComponent.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "소스 컴포넌트 설정을 찾을 수 없습니다."));
            ComponentSetting targetSetting = componentSettingRepository.findFirstByComponentIdOrderByVersionDesc(targetComponent.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "타겟 컴포넌트 설정을 찾을 수 없습니다."));
            Connection connection = Connection.builder()
                    .fromComponent(sourceComponent)
                    .toComponent(targetComponent)
                    .fromPort(sourceSetting.getPort())
                    .toPort(targetSetting.getPort())
                    .type(ConnectionType.valueOf(request.getConnectionType()))
                    .build();
            connectionRepository.save(connection);

            return new ConnectionCreateResponse(
                            connection.getId(), 
                            connection.getType().toString(),
                            connection.getToComponent().getId(),
                            connection.getFromComponent().getId(),
                            connection.getFromPort(),
                            connection.getToPort());
        } catch (CustomException e) {
            log.error("Custom error occurred while creating connection", e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating connection", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "컴포넌트 연결 생성 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public ConnectionDeleteResponse deleteConnection(Long projectId, Long sourceComponentId, Long connectionId, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        try {
            Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            Component sourceComponent = componentRepository.findByIdAndProjectUserIdAndProjectId(sourceComponentId, user.getId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "소스 컴포넌트를 찾을 수 없습니다."));
            Connection connection = connectionRepository.findByIdAndFromComponentId(connectionId, sourceComponent.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CONNECTION_NOT_FOUND, "연결을 찾을 수 없습니다."));
            connectionRepository.delete(connection);
        } catch (CustomException e) {
            log.error("Custom error occurred while deleting connection", e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting connection", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "연결 삭제 중 오류가 발생했습니다.");
        }
        return new ConnectionDeleteResponse("연결이 삭제되었습니다.");
    }
}
