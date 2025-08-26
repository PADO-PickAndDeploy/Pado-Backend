package org.pado.api.domain.deployment;

import java.time.LocalDateTime;
import java.util.List;

import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String deploymentId;
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
