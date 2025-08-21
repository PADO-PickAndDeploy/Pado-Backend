package org.pado.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.pado.api.domain.common.Status;
import org.pado.api.domain.component.ComponentType;
import org.pado.api.domain.component.ComponentSubType;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProjectDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String thumbnail;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ComponentInfo> components;


    @Getter
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentInfo {
        private Long id;
        private Long version;
        private String name;
        private ComponentType type;
        private ComponentSubType subtype;
        private String thumbnail;
        private LocalDateTime deployStartTime;
        private LocalDateTime deployEndTime;

        private List<ComponentInfo> children;
        private List<ConnectionInfo> connections;
    }
}
