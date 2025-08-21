package org.pado.api.domain.component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(
    name = "component_lists",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"type", "subtype"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@Entity
public class ComponentList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String description;
    private String thumbnail;
    private ComponentType type;
    private ComponentSubType subtype;
}
