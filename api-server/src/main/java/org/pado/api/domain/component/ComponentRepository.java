package org.pado.api.domain.component;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long> {
    List<Component> findByProjectId(Long projectId);
    List<Component> findByProjectIdAndParent(Long projectId, Component parent);
    List<Component> findByParentId(Long parentId);
    List<Component> findByNameContaining(String keyword);
    List<Component> findByType(ComponentType type);
    List<Component> findBySubtype(ComponentSubType subtype);
    Optional<Component> findByIdAndProjectUserId(Long id, Long userId);
}
