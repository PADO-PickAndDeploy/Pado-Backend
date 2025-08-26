package org.pado.api.domain.component;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentSettingRepository extends MongoRepository<ComponentSetting, String>  {
    Optional<ComponentSetting> findByComponentId(Long componentId);
}
