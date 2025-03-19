package com.liu.gymmanagement.repository;

import com.liu.gymmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);  // 根据用户名查找用户
    Optional<User> findByPhone(String phone);  // 根据手机号查找用户
    Optional<User> findByUserID(String userID);  // 根据用户名查找用户
}