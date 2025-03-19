package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 学生注册
    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody User user) {
        return registerUser(user, 1);  // 角色ID = 1 (学生)
    }

    // 管理员注册
    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        return registerUser(user, 2);  // 角色ID = 2 (管理员)
    }

    private ResponseEntity<?> registerUser(User user, int roleId) {
        try {
            // 打印接收到的用户数据，用于调试
            System.out.println("Received user data: " + user);

            // 基本验证
            if (user.getUserID() == null || user.getUserID().trim().isEmpty()) {
                return new ResponseEntity<>("UserID cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return new ResponseEntity<>("Username cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                return new ResponseEntity<>("Phone cannot be empty", HttpStatus.BAD_REQUEST);
            }

            User registeredUser = userService.registerUser(user,roleId);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // 打印详细错误信息到控制台
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 用户登录 API（学生和管理员共用）
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User loginUser, int roleId) {
        Optional<User> user = userService.loginUser(loginUser.getUserID(), loginUser.getPassword(), roleId);
        if (user.isPresent()) {
            return new ResponseEntity<>("Login successful", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }
}