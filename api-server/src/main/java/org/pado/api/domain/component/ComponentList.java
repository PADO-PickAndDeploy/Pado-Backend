package org.pado.api.domain.component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(name = "component_lists")
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
