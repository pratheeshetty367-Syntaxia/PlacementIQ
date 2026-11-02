package com.placementiq.entity;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Represents a security role that can be assigned to a User
 * (e.g. ROLE_USER, ROLE_ADMIN). Used by Spring Security for authorization.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Enumerated(EnumType.STRING)
    @Column(length = 30, unique = true, nullable = false)
    private RoleName name;
 
    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN
    }
}
 
