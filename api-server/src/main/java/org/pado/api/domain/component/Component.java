package org.pado.api.domain.component;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.pado.api.domain.common.BaseTimeEntity;
import org.pado.api.domain.connection.Connection;
import org.pado.api.domain.project.Project;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(
    name = "components",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id", "pid"}),
        @UniqueConstraint(columnNames = {"name", "pid"})
    }
)
@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Component parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> children = new ArrayList<>();

    @OneToMany(mappedBy = "fromComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> fromConnections = new ArrayList<>();

    @OneToMany(mappedBy = "toComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> toConnections = new ArrayList<>();

    private Long version;
    private String name;
    private ComponentType type;
    private ComponentSubType subtype;
    private String thumbnail;
    private ComponentDeploymentStatus deploymentStatus;
    private ComponentRunningStatus runningStatus;
    private LocalDateTime deployStartTime;
    private LocalDateTime deployEndTime;
}
