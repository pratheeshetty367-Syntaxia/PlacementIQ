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
import java.util.List;
 
/**
 * Stores the metadata of a single uploaded resume document belonging to a User.
 *
 * The binary file itself is written to the configured upload directory on disk
 * (see ResumeStorageService); only its metadata and the plain text extracted
 * from it are persisted in PostgreSQL. Each user may hold at most one resume,
 * which is enforced by the unique constraint on user_id.
 *
 * From Part 3 a resume also owns the ATS analyses that were run against it.
 */
@Entity
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resumes_user_id", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    /**
     * Owning side of the one-to-one relationship with User.
     * A resume can never exist without a user (optional = false).
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
 
    /** The file name exactly as it was on the user's machine, e.g. "Jane_Doe_Resume.pdf". */
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;
 
    /** The randomised, collision-free name actually used on disk. */
    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255)
    private String storedFileName;
 
    /** Absolute path of the stored file, used for download and deletion. */
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 10)
    private FileType fileType;
 
    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;
 
    /** Size of the stored file in bytes. */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
 
    /**
     * Plain text extracted from the document at upload time.
     * Mapped to a PostgreSQL TEXT column (instead of @Lob, which Hibernate would
     * map to a large-object OID on PostgreSQL and complicate reads).
     * Consumed by the ATS/matching modules in later parts.
     */
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;
 
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    /**
     * ADDED IN PART 3 - the ATS analyses that were run against this resume.
     * Without this cascade, deleting or replacing a resume would fail on the
     * ats_analyses.resume_id foreign key once any analysis exists. Declaring it
     * here means the Part 2 delete flow keeps working unchanged.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AtsAnalysis> analyses = new ArrayList<>();
 
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
 
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
 
    /** Document formats accepted by the platform. */
    public enum FileType {
        PDF,
        DOCX
    }
}
