package com.liu.gymmanagement.service;
import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.model.Role;
import com.liu.gymmanagement.model.UserRole;
import com.liu.gymmanagement.model.UserRoleId;
import com.liu.gymmanagement.repository.UserRepository;
import com.liu.gymmanagement.repository.RoleRepository;
import com.liu.gymmanagement.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用户注册
    public User registerUser(User user, int roleId) {
        try {
            System.out.println("Starting user registration process...");

            // 打印所有可用的角色
            System.out.println("Available roles:");
            roleRepository.findAll().forEach(role -> 
                System.out.println("Role: " + role.getRoleName() + ", ID: " + role.getRoleID()));
            
            // 检查用户ID是否存在
            if (userRepository.findByUsername(user.getUserID()).isPresent()) {
                throw new RuntimeException("UserID already exists");
            }

            // 检查手机号是否存在
//            if (userRepository.findByPhone(user.getPhone()).isPresent()) {
//                throw new RuntimeException("Phone number already registered");
//            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 先保存用户
            User savedUser = userRepository.save(user);

            // 查找对应角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role ID " + roleId + " not found"));

//            // 将用户和角色关联，存入 user_role 表
//            UserRole userRole = new UserRole(savedUser.getUserID(), role.getRoleID());
//            userRoleRepository.save(userRole);

            // 创建 UserRoleId（复合主键）
            UserRoleId userRoleId = new UserRoleId(savedUser.getUserID(), role.getRoleID());

            // 创建 UserRole 并保存
            UserRole userRole = new UserRole(userRoleId);  // 使用复合主键的构造器
            userRoleRepository.save(userRole);

            return savedUser;
        } catch (Exception e) {
            System.out.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    // 用户登录
    public Optional<User> loginUser(String userID, String password, int roleId) {
        Optional<User> user = userRepository.findByUserID(userID);
//        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
//            // 检查用户是否有该角色
//            if (user.get().getRoles().contains(roleId)) {
//                return user;
//            }
//        }
//        return Optional.empty();  // 登录失败
        if (user.isPresent()) {
            System.out.println("User found: " + user.get().getUserID());
            System.out.println("Password matches: " + passwordEncoder.matches(password, user.get().getPassword()));
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                return user;
            }
        }
        return Optional.empty();
    }

    //实现查询用户信息
    public Optional<User> getUserInfo(String userID, int roleId) {
        // 查询用户
        Optional<User> userOpt = userRepository.findByUserID(userID);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 查询用户角色，确保用户拥有该角色
            List<UserRole> roles = userRoleRepository.findById_UserID(userID);
            boolean hasRole = roles.stream().anyMatch(r -> r.getRoleID() == roleId);

            if (hasRole) {
                return Optional.of(user);
            }
        }
        return Optional.empty();  // 用户不存在或者没有该角色
    }

    //实现修改用户信息
    public User updateUserInfo(String userID, int roleId, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findByUserID(userID);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // 确保用户有该角色
            List<UserRole> roles = userRoleRepository.findById_UserID(userID);
            boolean hasRole = roles.stream().anyMatch(r -> r.getRoleID() == roleId);

            if (!hasRole) {
                throw new RuntimeException("Unauthorized to update this user.");
            }

            // 只允许修改除 userID 和角色外的字段
            // 这里会对传入的字段进行判断，如果字段有修改就更新
            if (updatedUser.getUsername() != null) {
                existingUser.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getPhone() != null) {
                existingUser.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getAge() != 0) {  // 你可以根据实际业务规则来判断age
                existingUser.setAge(updatedUser.getAge());
            }
            if (updatedUser.getGender() != null) {
                existingUser.setGender(updatedUser.getGender());
            }
            if (updatedUser.getLabel() != null) {
                existingUser.setLabel(updatedUser.getLabel());
            }

            // 这里密码不能更新，如果用户提供了密码，则不做任何操作
            if (updatedUser.getPassword() != null) {
                throw new RuntimeException("Password cannot be updated.");
            }

            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found.");
        }
    }


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void testDatabaseConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }


}
