package org.pado.api.domain.component;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "component_settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ComponentSetting {
    @Id
    private Long id;

    private Long componentId;
    private Long version;
    private ComponentSubType type;
    private String value;
}
