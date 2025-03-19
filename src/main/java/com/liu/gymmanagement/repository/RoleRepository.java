package com.liu.gymmanagement.repository;

import com.liu.gymmanagement.model.Role;
//import com.liu.gymmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    // 根据角色名查找角色
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findById(Integer roleId);
}