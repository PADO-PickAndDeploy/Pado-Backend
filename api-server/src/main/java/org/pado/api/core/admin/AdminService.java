package org.pado.api.core.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.pado.api.domain.credential.Credential;
import org.pado.api.domain.credential.CredentialRepository;
import org.pado.api.domain.deployment.Deployment;
import org.pado.api.domain.deployment.DeploymentRepository;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.project.ProjectRunningStatus;
import org.pado.api.domain.user.User;
import org.pado.api.domain.user.UserRepository;
import org.pado.api.core.admin.dto.request.AdminSignupRequest;
import org.pado.api.core.admin.dto.response.AdminSignupResponse;
import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.rabbitmq.RabbitSendService;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.core.vault.service.CredentialVaultService;
import org.pado.api.core.vault.service.DeployVaultService;
import org.pado.api.domain.common.Status;
import org.pado.api.domain.component.Component;
import org.pado.api.domain.component.ComponentDefaultSetting;
import org.pado.api.dto.DeploymentMessage;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.request.ComponentSettingRequest;
import org.pado.api.dto.request.ConnectionCreateRequest;
import org.pado.api.dto.request.CredentialRegisterRequest;
import org.pado.api.dto.request.ProjectCreateRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentDeleteResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.pado.api.dto.response.ComponentSettingResponse;
import org.pado.api.dto.response.ConnectionCreateResponse;
import org.pado.api.dto.response.ConnectionDeleteResponse;
import org.pado.api.dto.response.CredentialDeleteResponse;
import org.pado.api.dto.response.CredentialDetailResponse;
import org.pado.api.dto.response.CredentialResponse;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.DeployStartResponse;
import org.pado.api.dto.response.DeployStopResponse;
import org.pado.api.dto.response.ProjectCreateResponse;
import org.pado.api.dto.response.ProjectDetailResponse;
import org.pado.api.dto.response.ProjectListResponse;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    
    private final CredentialRepository credentialRepository;
    private final CredentialVaultService credentialVaultService;
    
    private final ProjectRepository projectRepository;
    
    private final ComponentDefaultSettingRepository componentDefaultSettingRepository;
    private final ConnectionRepository connectionRepository;
    private final ComponentRepository componentRepository;
    private final ComponentSettingRepository componentSettingRepository;
    private final ComponentListRepository componentListRepository;
    
    private final DeploymentRepository deploymentRepository;
    private final DeployVaultService deployVaultService;
    private final RabbitSendService rabbitSendService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** ----------------- Auth ----------------- */
    @Transactional
    public AdminSignupResponse signup(AdminSignupRequest request){
        User user = userRepository.save(
            User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build()
        );
        return new AdminSignupResponse(user.getId(), "회원가입 성공");

    }

    /** ----------------- Credential ----------------- */
    @Transactional
    public CredentialResponse createCredential(Long userId, CredentialRegisterRequest request){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Credential credential = credentialRepository.save(
            Credential.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .build()
        );

        try {
            // Vault에 실제 credentialData 저장
            credentialVaultService.storeCredentialData(user, credential, request.getData());
            log.info("Successfully stored credential data in Vault for credential: {}", credential.getId());
        } catch (Exception e) {
            // Vault 저장 실패시 DB 롤백을 위해 RuntimeException 던지기
            log.error("Failed to store credential data in Vault for credential: {}", credential.getId(), e);
            throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED, 
                "크리덴셜 데이터 저장에 실패했습니다.", e);
        }

        return new CredentialResponse(credential.getId(),
                credential.getName(),
                credential.getType(),
                credential.getDescription(),
                "크리덴셜 등록 완료",
                credential.getCreatedAt().format(formatter)
        );
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getAllCredentials(Long userId) {
        log.info("Retrieving all credentials for user: {}", userId);
        
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return credentialRepository.findByUser(user).stream()
                .map(c -> new CredentialResponse(
                        c.getId(),
                        c.getName(),
                        c.getType(),
                        c.getDescription(),
                        "크리덴셜 조회 완료",
                        c.getCreatedAt().format(formatter)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CredentialDetailResponse getCredential(Long userId, Long credentialId){
        log.info("Getting credential detail: {} for user: {}", credentialId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 크리덴셜 존재 여부
        Credential credential = credentialRepository.findById(credentialId)
            .orElseThrow(() -> new CustomException(ErrorCode.CREDENTIAL_NOT_FOUND));
        // 사용자 소유권 확인
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        // Vault에서 실제 데이터 조회
        String credentialData;
        try {
            credentialData = credentialVaultService.getCredentialData(user, credential);
            log.debug("Successfully retrieved credential data from Vault for credential: {}", credentialId);
        } catch (Exception e) {
            log.error("Failed to retrieve credential data from Vault for credential: {}", credentialId, e);
            throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED, 
                "크리덴셜 데이터 조회에 실패했습니다.", e);
        }

        return new CredentialDetailResponse(
                credential.getId(),
                credential.getName(),
                credential.getType(),
                credential.getDescription(),
                credentialData,
                "크리덴셜 상세 조회 완료",
                credential.getCreatedAt().format(formatter),
                credential.getUpdatedAt().format(formatter)
        );
    }

    @Transactional
    public CredentialDeleteResponse deleteCredential(Long userId, Long credentialId){
        log.info("Deleting credential: {} for user: {}", credentialId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Credential credential;

        try {
            credential = credentialRepository.findByIdAndUserId(credentialId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CREDENTIAL_NOT_FOUND));

            // 추후 개발 예정 부분 (크리덴셜을 사용하는 프로젝트 상태 확인)
            // List<Project> projectsUsingCredential = findProjectsUsingCredential(credentialId);
            // for (Project project : projectsUsingCredential) {
            //     if (project.getStatus() != Status.DRAFT && project.getStatus() != Status.STOP) {
            //         throw new CustomException(ErrorCode.CREDENTIAL_DELETION_NOT_ALLOWED, 
            //             "실행 중이거나 배포된 프로젝트에서 사용 중인 크리덴셜입니다.");
            //     }
            // }

            // Vault에서 크리덴셜 삭제
            try {
                credentialVaultService.deleteCredentialData(user, credential);
                log.info("Successfully deleted credential data from Vault for credential: {}", credentialId);
            } catch (Exception e) {
                log.error("Failed to delete credential data from Vault for credential: {}", credentialId, e);
                throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED,
                    "Vault에서 크리덴셜 데이터 삭제에 실패했습니다.", e);
            }

            // DB에서 크리덴셜 삭제
            credentialRepository.delete(credential);

        } catch (CustomException e) {
            log.warn("Credential deletion failed for user: {}, credential ID: {}", user.getId(), credentialId);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting credential for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "크리덴셜 삭제 중 오류가 발생했습니다.");
        }
        
        return new CredentialDeleteResponse("크리덴셜이 성공적으로 삭제되었습니다.");
    } 

    /** ----------------- Project ----------------- */
    private static void validateComponentCreation(Project project) {
        if (project.getDeploymentStatus() != ProjectDeploymentStatus.DRAFT &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.TERMINATED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.DEPLOYED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트가 실행 중이거나 배포 상태입니다. 설정을 변경할 수 없습니다.");
        }
    }

    @Transactional
    public ProjectCreateResponse createProject(ProjectCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
    public ProjectListResponse getProjects(Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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
    public ProjectDetailResponse getProjectDetail(Long id, Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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
    public DefaultResponse deleteProject(Long id, Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Project project;

        try {
            project = projectRepository.findByIdAndUserIdForUpdate(id, user.getId())
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

    /** ----------------- Component ----------------- */
    private String generateUniqueName(String name) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8).toLowerCase();
        return name + "-" + randomSuffix;
    }

    // private static void validateComponentCreation(Project project) {
    //     if (project.getDeploymentStatus() != ProjectDeploymentStatus.DRAFT &&
    //         project.getDeploymentStatus() != ProjectDeploymentStatus.TERMINATED &&
    //         project.getDeploymentStatus() != ProjectDeploymentStatus.DEPLOYED &&
    //         project.getDeploymentStatus() != ProjectDeploymentStatus.FAILED) {
    //         throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트가 실행 중이거나 배포 상태입니다. 설정을 변경할 수 없습니다.");
    //     }
    // }
    
    @Transactional(readOnly = true)
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
    public ComponentCreateResponse createComponent(Long projectId, ComponentCreateRequest request, Long userId) {
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 삭제 불가)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

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
                        parentComponent.getType(),
                        parentComponent.getSubtype(),
                        parentComponent.getName(),
                        parentComponent.getThumbnail(),
                        parentComponent.getDeploymentStatus(),
                        parentComponent.getRunningStatus()
                ),
                new ComponentCreateResponse.ComponentCreateInfo(
                        component.getId(),
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
    public ComponentSettingResponse setComponentSetting(Long projectId, Long componentId, ComponentSettingRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 수정 불가)
        try {
            projectRepository.findByIdAndUserId(projectId, user.getId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
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
            ComponentSetting componentSetting = componentSettingRepository.findByComponentId(component.getId())
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
    public ComponentDeleteResponse deleteComponent(Long projectId, Long componentId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 프로젝트 및 컴포넌트가 실행 중인지 여부 확인 후 에러 처리 필요 (상태가 DRAFT, STOP이 아닌 경우 삭제 불가)
        try {
            Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            componentRepository.findByIdAndProjectUserId(componentId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "컴포넌트를 찾을 수 없습니다."));
            ComponentSetting componentSettings = componentSettingRepository.findByComponentId(componentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "컴포넌트 설정을 찾을 수 없습니다."));
            componentSettingRepository.delete(componentSettings);
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
    public ConnectionCreateResponse createConnection(Long projectId, Long sourceComponentId, ConnectionCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        try {
            Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
            validateComponentCreation(project);
            Component sourceComponent = componentRepository.findByIdAndProjectUserIdAndProjectId(sourceComponentId, user.getId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "소스 컴포넌트를 찾을 수 없습니다."));
            Component targetComponent = componentRepository.findByIdAndProjectUserIdAndProjectId(request.getTargetComponentId(), user.getId(), project.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "타겟 컴포넌트를 찾을 수 없습니다."));

            // Source Component, Target Component에 대해서 연결이 가능한지 여부 확인 필요 (예: Service -> Service, Resource 불가능, React -> Spring, Spring -> MySQL)
            ComponentSetting sourceSetting = componentSettingRepository.findByComponentId(sourceComponent.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "소스 컴포넌트 설정을 찾을 수 없습니다."));
            ComponentSetting targetSetting = componentSettingRepository.findByComponentId(targetComponent.getId())
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
    public ConnectionDeleteResponse deleteConnection(Long projectId, Long sourceComponentId, Long connectionId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        try {
            Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
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

    /** ----------------- Deploy ----------------- */
    @Transactional
    public DeployStartResponse startDeployment(Long projectId, Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"프로젝트를 찾을 수 없습니다."));

        if (project.getDeploymentStatus() != ProjectDeploymentStatus.DRAFT &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.TERMINATED) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트 상태가 올바르지 않습니다.");
        }
        List<Component> components = componentRepository.findByProjectIdForUpdate(project.getId());
        LocalDateTime requestTime = LocalDateTime.now();
        List<Deployment.ComponentInfo> componentInfos;

        try {
        componentInfos = componentRepository.findByProjectIdAndParent(project.getId(), null).stream()
                .map(component -> new Deployment.ComponentInfo(
                    component.getId(),
                    component.getName(),
                    component.getType(),
                    component.getSubtype(),
                    component.getChildren().stream()
                        .map(child -> new Deployment.ComponentInfo(
                            child.getId(),
                            child.getName(),
                            child.getType(),
                            child.getSubtype(),
                            null,
                            connectionRepository.findByFromComponent(child).stream()
                                                    .map(conn -> new Deployment.ConnectionInfo(
                                                            conn.getId(),
                                                            conn.getFromComponent().getId(),
                                                            conn.getToComponent().getId(),
                                                            conn.getFromPort(),
                                                            conn.getToPort()
                                                    )).collect(Collectors.toList()),
                            componentSettingRepository.findByComponentId(child.getId())
                                    .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "컴포넌트 설정을 찾을 수 없습니다."))
                                    .getValue()
                        ))
                        .collect(Collectors.toList()),
                    connectionRepository.findByFromComponent(component).stream()
                                    .map(conn -> new Deployment.ConnectionInfo(
                                            conn.getId(),
                                            conn.getFromComponent().getId(),
                                            conn.getToComponent().getId(),
                                            conn.getFromPort(),
                                            conn.getToPort()
                                    )).collect(Collectors.toList()),
                    componentSettingRepository.findByComponentId(component.getId())
                            .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_SETTING_NOT_FOUND, "컴포넌트 설정을 찾을 수 없습니다."))
                            .getValue()
                ))
                .collect(Collectors.toList());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "배포 중 알 수 없는 오류가 발생했습니다.", e);
        }
        project.setRunningStatus(ProjectRunningStatus.UNKNOWN);
        project.setDeploymentStatus(ProjectDeploymentStatus.QUEUED);
        projectRepository.save(project);
        components.forEach(component -> {
            component.setDeploymentStatus(ComponentDeploymentStatus.QUEUED);
            component.setRunningStatus(ComponentRunningStatus.UNKNOWN);
        });
        componentRepository.saveAll(components);

        Deployment deployment = Deployment.builder()
                .projectId(project.getId())
                .createdAt(requestTime)
                .components(componentInfos)
                .deploymentId(UUID.randomUUID().toString())
                .build();

        deploymentRepository.save(deployment);
        
        String goRoleName = "go-role";
        String wrappedToken = deployVaultService.issueWrappedSecretId(goRoleName, 60);
        DeploymentMessage message = new DeploymentMessage("start", wrappedToken, deployment.getDeploymentId(), deployment);

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메시지 직렬화 중 오류가 발생했습니다.", e);
        }
        rabbitSendService.sendStart(json);

        return new DeployStartResponse(requestTime, "Deployment started for project ID: " + projectId, deployment.getDeploymentId());
    }

    @Transactional
    public DeployStopResponse stopDeployment(Long projectId, Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"프로젝트를 찾을 수 없습니다."));
        if (project.getDeploymentStatus() != ProjectDeploymentStatus.QUEUED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.FAILED &&
            project.getDeploymentStatus() != ProjectDeploymentStatus.DEPLOYING) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS, "프로젝트 상태가 올바르지 않습니다.");
        }
        Deployment deployment = deploymentRepository.findTopByProjectIdOrderByCreatedAtDesc(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND, "진행 중인 배포를 찾을 수 없습니다."));
        List<Component> components = componentRepository.findByProjectIdForUpdate(project.getId());

        project.setDeploymentStatus(ProjectDeploymentStatus.STOP_REQUESTED);
        components.forEach(component -> {
            component.setDeploymentStatus(ComponentDeploymentStatus.STOP_REQUESTED);
        });
        LocalDateTime requestTime = LocalDateTime.now();

        // Stop the deployment logic here
        String goRoleName = "go-role";
        String wrappedToken = deployVaultService.issueWrappedSecretId(goRoleName, 60);
        DeploymentMessage message = new DeploymentMessage("stop", wrappedToken, deployment.getDeploymentId(), null);

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메시지 직렬화 중 오류가 발생했습니다.", e);
        }
        rabbitSendService.sendStop(json);

        return new DeployStopResponse(requestTime, "Deployment stopped for project ID: " + projectId, deployment.getDeploymentId());
    }

    /** ----------------- Default Setting ----------------- */
    public ComponentDefaultSetting createDefaultSetting(ComponentDefaultSetting request) {
        // 그대로 저장 (Raw 데이터)
        return componentDefaultSettingRepository.save(request);
    }

    public List<ComponentDefaultSetting> getAllDefaultSettings() {
        return componentDefaultSettingRepository.findAll();
    }

    public ComponentDefaultSetting getDefaultSetting(String id) {
        // 존재하지 않으면 null 반환 (에러 처리 없음)
        return componentDefaultSettingRepository.findById(id).orElse(null);
    }

    public ComponentDefaultSetting updateDefaultSetting(String id, ComponentDefaultSetting setting) {
        // 그냥 id 덮어씌우고 저장
        setting.setId(id);
        return componentDefaultSettingRepository.save(setting);
    }

    public DefaultResponse deleteDefaultSetting(String id) {
        componentDefaultSettingRepository.deleteById(id);
        return new DefaultResponse("Deleted successfully");
    }

    /** ----------------- Component List ----------------- */
    @Transactional
    public ComponentList createComponentList(ComponentList request) {
        return componentListRepository.save(request);
    }

    @Transactional(readOnly = true)
    public ComponentList getComponentListDetail(Long id) {
        return componentListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ComponentList not found with id: " + id));
    }

    @Transactional
    public ComponentList updateComponentList(Long id, ComponentList request) {
        ComponentList existing = getComponentListDetail(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setResourceThumbnail(request.getResourceThumbnail());
        existing.setServiceThumbnail(request.getServiceThumbnail());
        existing.setResourceType(request.getResourceType());
        existing.setServiceType(request.getServiceType());
        return componentListRepository.save(existing);
    }

    @Transactional
    public DefaultResponse deleteComponentList(Long id) {
        componentListRepository.delete(getComponentListDetail(id));
        return new DefaultResponse("ComponentList가 삭제되었습니다.");
    }

    /** ----------------- Connection ----------------- */
    // 특정 유저의 특정 프로젝트의 컴포넌트 연결을 전체 조회하는 기능
    @Transactional(readOnly = true)
    public List<Connection> getAllConnections(Long userId, Long projectId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        List<Component> components = componentRepository.findByProjectId(projectId);

        List<Connection> connections = new ArrayList<>();
        // ConnectionRepository에서 findAllByProjectId(projectId)로 대체 가능
        for (Component c : components) {
            connections.addAll(connectionRepository.findByFromComponent(c));
            connections.addAll(connectionRepository.findByToComponent(c));
        }
        return connections;
    }

    @Transactional(readOnly = true)
    public Connection getConnection(Long userId, Long projectId, Long connectionId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        return connectionRepository.findById(connectionId)
                .filter(conn -> conn.getFromComponent().getProject().getId().equals(projectId) &&
                                conn.getFromComponent().getProject().getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.CONNECTION_NOT_FOUND));
    }

    @Transactional
    public Connection updateConnection(Long userId, Long projectId, Long connectionId, Connection request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        Connection existing = connectionRepository.findById(connectionId)
                .filter(conn -> conn.getFromComponent().getProject().getId().equals(projectId))
                .orElseThrow(() -> new CustomException(ErrorCode.CONNECTION_NOT_FOUND));

        Component from = componentRepository.findById(request.getFromComponent().getId())
                .filter(c -> c.getProject().getId().equals(projectId))
                .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "From Component가 유효하지 않습니다."));

        Component to = componentRepository.findById(request.getToComponent().getId())
                .filter(c -> c.getProject().getId().equals(projectId))
                .orElseThrow(() -> new CustomException(ErrorCode.COMPONENT_NOT_FOUND, "To Component가 유효하지 않습니다."));

        // 수정
        existing.setFromComponent(from);
        existing.setToComponent(to);
        existing.setType(request.getType());
        existing.setFromPort(request.getFromPort());
        existing.setToPort(request.getToPort());

        return connectionRepository.save(existing);
    }


}
