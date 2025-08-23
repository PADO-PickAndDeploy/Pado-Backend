package org.pado.api.domain.component;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "component_default_settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ComponentDefaultSetting {
    @Id
    private Long id;

    private ComponentSubType type;
    private String value;
}
