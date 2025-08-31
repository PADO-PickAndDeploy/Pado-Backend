package org.pado.api.domain.component;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long> {
    List<Component> findByProjectId(Long projectId);
    List<Component> findByProjectIdAndParent(Long projectId, Component parent);
    List<Component> findByParentId(Long parentId);
    List<Component> findByNameContaining(String keyword);
    List<Component> findByType(ComponentType type);
    List<Component> findBySubtype(ComponentSubType subtype);
    Optional<Component> findByIdAndProjectId(Long id, Long projectId);
    Optional<Component> findByIdAndProjectUserId(Long id, Long userId);
    Optional<Component> findByIdAndProjectUserIdAndProjectId(Long id, Long userId, Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Component c WHERE c.project.id = :projectId")
    List<Component> findByProjectIdForUpdate(Long projectId);
}
