package ru.practicum.stats.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Event> events = ConcurrentHashMap.newKeySet();
}