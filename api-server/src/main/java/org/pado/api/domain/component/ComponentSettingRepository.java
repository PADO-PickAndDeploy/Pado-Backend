package org.pado.api.domain.component;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentSettingRepository extends MongoRepository<ComponentSetting, Long>  {
    Optional<ComponentSetting> findFirstByComponentIdOrderByVersionDesc(Long componentId);
    List<ComponentSetting> findByComponentId(Long componentId);
}
