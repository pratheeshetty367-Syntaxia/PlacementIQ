package com.placementiq.entity;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 * Tracks a single user's preparation progress for a specific topic/module
 * (e.g. "DSA - Arrays", "Aptitude - Time & Work", "Mock Interview - HR").
 * Many rows per user; used to power dashboard/progress analytics.
 */
@Entity
@Table(name = "placement_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementProgress {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    @Column(nullable = false, length = 150)
    private String moduleName;      // e.g. "Data Structures & Algorithms"
 
    @Column(nullable = false, length = 150)
    private String topicName;       // e.g. "Arrays & Strings"
 
    @Column(nullable = false)
    @Builder.Default
    private Integer completionPercentage = 0;
 
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.NOT_STARTED;
 
    private LocalDateTime lastUpdated;
 
    @PrePersist
    @PreUpdate
    protected void touch() {
        this.lastUpdated = LocalDateTime.now();
    }
 
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
}
 
