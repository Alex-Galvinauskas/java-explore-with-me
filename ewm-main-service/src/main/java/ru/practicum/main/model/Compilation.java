package ru.practicum.main.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, unique = true, length = 50)
    private String title;

    @Column(name = "pinned", nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @Builder.Default
    private Set<Event> events = ConcurrentHashMap.newKeySet();
}