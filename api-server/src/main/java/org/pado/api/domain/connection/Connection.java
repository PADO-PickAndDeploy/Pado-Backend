package org.pado.api.domain.connection;

import org.pado.api.domain.common.BaseTimeEntity;
import org.pado.api.domain.component.Component;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Component fromComponent;

    @ManyToOne(fetch = FetchType.LAZY)
    private Component toComponent;

    private ConnectionType type;
    private Long fromPort;
    private Long toPort;
}
