package org.pado.api.domain.component;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentDefaultSettingRepository extends MongoRepository<ComponentDefaultSetting, Long> {
    Optional<ComponentDefaultSetting> findByType(ComponentSubType type);
}
