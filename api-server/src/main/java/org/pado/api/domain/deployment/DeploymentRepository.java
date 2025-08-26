package org.pado.api.domain.deployment;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeploymentRepository extends MongoRepository<Deployment, String>{
    Optional<Deployment> findTopByProjectIdOrderByCreatedAtDesc(Long projectId);
}
