package com.placementiq.entity;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
/**
 * Core account entity used for authentication.
 * Stores credentials and links to roles (authorization),
 * to a one-to-one UserProfile (personal/placement details),
 * from Part 2 to a one-to-one Resume, and from Part 3 to the
 * ATS analyses that have been run on that resume.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 100)
    private String fullName;
 
    @Column(nullable = false, unique = true, length = 150)
    private String email;
 
    @JsonIgnore
    @Column(nullable = false)
    private String password;
 
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
 
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    private LocalDateTime updatedAt;
 
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
 
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile profile;
 
    /**
     * ADDED IN PART 2 - inverse (non-owning) side of the User <-> Resume
     * one-to-one relationship. The foreign key lives on the resumes table;
     * this mapping exists so that deleting a User also removes their resume
     * row instead of failing on the foreign key constraint. Marked @JsonIgnore
     * so serialising a User never drags the resume (and its extracted text)
     * into an API response.
     */
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Resume resume;
 
    /**
     * ADDED IN PART 3 - every ATS analysis this user has run. The foreign key
     * lives on ats_analyses.user_id; this inverse mapping exists so deleting a
     * User also removes their analysis history instead of failing on the foreign
     * key constraint. @JsonIgnore keeps the (potentially long) history out of any
     * response that happens to serialise a User.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AtsAnalysis> atsAnalyses = new ArrayList<>();
 
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
 
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
