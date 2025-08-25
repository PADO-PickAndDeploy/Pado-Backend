package org.pado.api.domain.deployment;

import java.time.LocalDateTime;
import java.util.List;

import org.pado.api.domain.component.ComponentDeploymentStatus;
import org.pado.api.domain.component.ComponentRunningStatus;
import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.project.ProjectDeploymentStatus;
import org.pado.api.domain.project.ProjectRunningStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Deployment {
    @Id
    private String id;
    private Long projectId;
    private LocalDateTime createdAt;

    private List<ComponentInfo> components;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionInfo {
        Long id;
        Long fromComponentId;
        Long toComponentId;
        Long fromPort;
        Long toPort;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentInfo {
        private Long id;
        private String name;
        private ComponentType type;
        private ComponentSubType subtype;
        private List<ComponentInfo> children;
        private List<ConnectionInfo> connections;
        private String settingJson;
    }
}
