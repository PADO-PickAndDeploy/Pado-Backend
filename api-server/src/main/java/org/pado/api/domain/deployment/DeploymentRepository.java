package org.pado.api.domain.deployment;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeploymentRepository extends MongoRepository<Deployment, String>{

}
