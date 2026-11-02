package com.placementiq.repository;
 
import com.placementiq.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
/**
 * Data access layer for the Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
 
    Optional<Role> findByName(Role.RoleName name);
}
 
