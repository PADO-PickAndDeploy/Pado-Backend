package org.pado.api.dto.response;

import java.util.List;

import org.pado.api.domain.component.ComponentSubType;
import org.pado.api.domain.component.ComponentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ComponentListResponse {
    private List<ComponentInfo> components;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentInfo {
        private Long id;
        private String name;
        private String description;
        private String thumbnail;
        private ComponentType type;
        private ComponentSubType subtype;
    }
}
