package com.liu.gymmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.liu.gymmanagement.model.UserRole;
import com.liu.gymmanagement.model.UserRoleId;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    // 按userID查询，这里需要根据复合主键UserRoleId的userID字段来查询
    List<UserRole> findById_UserID(String userID);


}
