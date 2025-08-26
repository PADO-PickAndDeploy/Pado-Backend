package org.pado.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.rabbitmq.RabbitSendService;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.core.vault.service.DeployVaultService;
import org.pado.api.domain.component.Component;
import org.pado.api.domain.component.ComponentDeploymentStatus;
import org.pado.api.domain.component.ComponentRepository;
import org.pado.api.domain.component.ComponentRunningStatus;
import org.pado.api.domain.component.ComponentSettingRepository;
import org.pado.api.domain.connection.ConnectionRepository;
import org.pado.api.domain.deployment.Deployment;
import org.pado.api.domain.deployment.DeploymentRepository;
import org.pado.api.domain.project.Project;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRepository;
import org.pado.api.domain.project.ProjectRunningStatus;
import org.pado.api.domain.user.User;
import org.pado.api.dto.DeploymentMessage;
import org.pado.api.dto.response.DeployStartResponse;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeployService {
    private final ProjectRepository projectRepository;
    private final DeploymentRepository deploymentRepository;
    private final ComponentRepository componentRepository;
    private final ComponentSettingRepository componentSettingRepository;
    private final ConnectionRepository connectionRepository;
    private final DeployVaultService deployVaultService;
    private final RabbitSendService rabbitSendService;
    private final ObjectMapper objectMapper;

    @Transactional
    public DeployStartResponse startDeployment(Long projectId, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Project project = projectRepository.findByIdAndUserIdForUpdate(projectId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"프로젝트를 찾을 수 없습니다."));
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
                .build();

        deploymentRepository.save(deployment);
        
        String goRoleName = "go-role";
        String wrappedToken = deployVaultService.issueWrappedSecretId(goRoleName, 60);
        DeploymentMessage message = new DeploymentMessage(wrappedToken, deployment);

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메시지 직렬화 중 오류가 발생했습니다.", e);
        }
        rabbitSendService.sendDefault(json);

        return new DeployStartResponse(requestTime, "Deployment started for project ID: " + projectId);
    }
}
