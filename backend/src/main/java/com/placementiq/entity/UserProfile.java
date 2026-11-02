package com.placementiq.entity;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Extended profile information for a User: academic and career details
 * used to personalize AI-driven placement preparation content.
 * Linked one-to-one with User via a shared foreign key (user_id).
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
 
    private String college;
 
    private String branch;
 
    private Integer graduationYear;
 
    @Column(length = 2000)
    private String skills;          // comma separated skill list, e.g. "Java,DSA,SQL"
 
    private String targetRole;      // e.g. "SDE-1", "Data Analyst"
 
    private String resumeUrl;
 
    private String phoneNumber;
 
    @Column(length = 1000)
    private String bio;
}
 
