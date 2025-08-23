package org.pado.api.domain.connection;

import org.pado.api.domain.component.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByFromComponent(Component fromComponent);
    List<Connection> findByToComponent(Component toComponent);
    List<Connection> findByFromPort(Long fromPort);
    List<Connection> findByToPort(Long toPort);
    Optional<Connection> findByIdAndFromComponentId(Long id, Long fromComponentId);
}
