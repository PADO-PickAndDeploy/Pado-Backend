package org.pado.api.domain.credential;

import java.util.List;
import java.util.Optional;

import org.pado.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    List<Credential> findByUser(User user);
    List<Credential> findByUserId(Long userId);
    List<Credential> findByName(String name);    
    List<Credential> findByType(CredentialType type);
    Optional<Credential> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUser(String name, User user);
}
