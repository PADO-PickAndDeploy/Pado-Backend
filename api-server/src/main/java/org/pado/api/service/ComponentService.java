package org.pado.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.domain.component.ComponentListRepository;
import org.pado.api.dto.response.ComponentListResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {
    private final ComponentListRepository componentListRepository;

    public ComponentListResponse getComponentList() {
        List<ComponentListResponse.ComponentInfo> components = componentListRepository.findAll().stream()
                .map(component -> new ComponentListResponse.ComponentInfo(
                        component.getId(),
                        component.getName(),
                        component.getDescription(),
                        component.getThumbnail(),
                        component.getType(),
                        component.getSubtype())
        ).collect(Collectors.toList());

        return new ComponentListResponse(components);
    }
}
