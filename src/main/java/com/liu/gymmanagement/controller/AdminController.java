package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // 管理员注册
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        return registerUser(user, 2);  // 角色ID = 2 (管理员)
    }

    // 管理员登录
    @PostMapping("/login")
    public ResponseEntity<String> loginAdmin(@RequestBody User loginUser) {
        return loginUser(loginUser, 2);  // 角色ID = 2 (管理员)
    }

    @GetMapping("/profile/{userID}")
    public ResponseEntity<User> getAdminProfile(@PathVariable String userID) {
        Optional<User> user = userService.getUserInfo(userID, 2); // 管理员角色 ID = 2
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{userID}")
    public ResponseEntity<?> updateAdminProfile(@PathVariable String userID, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUserInfo(userID, 2, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 注册用户（公用方法）
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

    // 登录用户（公用方法）
    private ResponseEntity<String> loginUser(User loginUser, int roleId) {
        Optional<User> user = userService.loginUser(loginUser.getUserID(), loginUser.getPassword(), roleId);
        if (user.isPresent()) {
            return new ResponseEntity<>("Login successful", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }
}
