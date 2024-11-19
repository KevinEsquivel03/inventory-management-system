package com.personal.inventory.repository;

import com.personal.inventory.entity.ERole;
import com.personal.inventory.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}