package org.pado.api.domain.component;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentListRepository extends JpaRepository<ComponentList, Long> {
    Optional<ComponentList> findByTypeAndSubtype(ComponentType type, ComponentSubType subtype);
}
