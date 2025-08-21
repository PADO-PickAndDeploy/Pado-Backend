package org.pado.api.domain.project;

import org.pado.api.domain.common.BaseTimeEntity;
import org.pado.api.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "projects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"uid", "name"})
})
@lombok.Setter
@lombok.Getter
@lombok.NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseTimeEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private User user;
    private String name;
    private String description;
    private String thumbnail;
}
