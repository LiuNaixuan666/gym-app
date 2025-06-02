package com.liu.gymmanagement.service;
import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.model.Role;
import com.liu.gymmanagement.model.UserRole;
import com.liu.gymmanagement.model.UserRoleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.liu.gymmanagement.repository.UserRepository;
import com.liu.gymmanagement.repository.RoleRepository;
import com.liu.gymmanagement.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

//    @Autowired
//    private StringRedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailCodeCache localCache;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // 用户注册
    public User registerUser(User user, int roleId, String verificationCode) {
        try {
            // 日志记录
            logger.info("Starting user registration for email: {}", user.getEmail());

            // 校验验证码
//            String codeInRedis = redisTemplate.opsForValue().get("email:code:" + user.getEmail());
//            if (codeInRedis == null) {
//                throw new RuntimeException("Verification code has expired or is invalid.");
//            }
//            if (!codeInRedis.equals(verificationCode)) {
//                throw new RuntimeException("Invalid verification code.");
//            }
            // ✅ 从本地缓存中获取验证码
            String codeInCache = localCache.get(user.getEmail());
            if (codeInCache == null) {
                throw new RuntimeException("Verification code has expired or is invalid.");
            }
            if (!codeInCache.equals(verificationCode)) {
                throw new RuntimeException("Invalid verification code.");
            }

            // 检查用户ID是否已存在
            if (userRepository.findByUsername(user.getUserID()).isPresent()) {
                throw new RuntimeException("UserID already exists");
            }

            // 检查手机号是否已注册
            if (userRepository.findByPhone(user.getPhone()).isPresent()) {
                throw new RuntimeException("Phone number already registered");
            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 保存用户
            User savedUser = userRepository.save(user);

            // 分配角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role ID " + roleId + " not found"));

            // 保存用户角色
            UserRoleId userRoleId = new UserRoleId(savedUser.getUserID(), role.getRoleID());
            UserRole userRole = new UserRole(userRoleId);
            userRoleRepository.save(userRole);

            // 将用户邮箱标记为已验证
            savedUser.setEmailVerified(true);
            userRepository.save(savedUser);

            logger.info("User registered successfully with userID: {}", savedUser.getUserID());
            return savedUser;

        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("Error during user registration: " + e.getMessage());
        }
    }


    // 用户登录（修改后支持JWT）
    public Optional<User> loginUser(String userID, String password, int roleId) {
        Optional<User> user = userRepository.findByUserID(userID);

        if (user.isPresent()) {
            // 调试信息
            System.out.println("User found: " + user.get().getUserID());
            System.out.println("Password matches: " + passwordEncoder.matches(password, user.get().getPassword()));

            // 验证密码
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                // 检查用户是否有该角色
                List<UserRole> roles = userRoleRepository.findById_UserID(userID);
                boolean hasRole = roles.stream().anyMatch(r -> r.getRoleID() == roleId);

                if (hasRole) {
                    return user;
                }
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

    // 修改用户信息
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

            // 更新可修改字段
            if (updatedUser.getUsername() != null) {
                existingUser.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getPhone() != null) {
                // 检查新手机号是否已被其他用户使用
                Optional<User> phoneUser = userRepository.findByPhone(updatedUser.getPhone());
                if (phoneUser.isPresent() && !phoneUser.get().getUserID().equals(userID)) {
                    throw new RuntimeException("Phone number already in use by another user");
                }
                existingUser.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getAge() != 0) {
                existingUser.setAge(updatedUser.getAge());
            }
            if (updatedUser.getGender() != null) {
                existingUser.setGender(updatedUser.getGender());
            }
            if (updatedUser.getLabel() != null) {
                existingUser.setLabel(updatedUser.getLabel());
            }

            // 如果提供了新密码，则加密后更新
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
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

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendEmailVerificationCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("3405508716@qq.com"); // 必须与spring.mail.username一致
            message.setTo(email);
            message.setSubject("Gym App Email Verification");
            message.setText("Your verification code is: <b>" + code + "</b><br/>This code is valid for 10 minutes.");

            // 发送邮件
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Error occurred while sending verification email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email.");
        }
    }

}






//    // 用户注册
//    public User registerUser(User user, int roleId, String verificationCode) {
//        try {
//            System.out.println("Starting user registration process...");
//
//            // 校验验证码
//            String codeInRedis = redisTemplate.opsForValue().get("email:code:" + user.getEmail());
//            if (codeInRedis == null || !codeInRedis.equals(verificationCode)) {
//                throw new RuntimeException("Invalid or expired verification code");
//            }
//
//            // 打印所有可用的角色
////            System.out.println("Available roles:");
////            roleRepository.findAll().forEach(role ->
////                System.out.println("Role: " + role.getRoleName() + ", ID: " + role.getRoleID()));
////
//            // 检查用户ID是否存在
//            if (userRepository.findByUsername(user.getUserID()).isPresent()) {
//                throw new RuntimeException("UserID already exists");
//            }
//
//            //检查手机号是否存在
//            if (userRepository.findByPhone(user.getPhone()).isPresent()) {
//                throw new RuntimeException("Phone number already registered");
//            }
//
//            // 加密密码
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//            // 先保存用户
//            User savedUser = userRepository.save(user);
//
//
//
//            // 查找对应角色
//            Role role = roleRepository.findById(roleId)
//                    .orElseThrow(() -> new RuntimeException("Role ID " + roleId + " not found"));
//
////            // 将用户和角色关联，存入 user_role 表
////            UserRole userRole = new UserRole(savedUser.getUserID(), role.getRoleID());
////            userRoleRepository.save(userRole);
//
//            // 创建 UserRoleId（复合主键）
//            UserRoleId userRoleId = new UserRoleId(savedUser.getUserID(), role.getRoleID());
//
//            // 创建 UserRole 并保存
//            UserRole userRole = new UserRole(userRoleId);  // 使用复合主键的构造器
//            userRoleRepository.save(userRole);
//
//            // 将用户邮箱标记为已验证
//            savedUser.setEmailVerified(true);
//            userRepository.save(savedUser);
//
////            // === 添加发送邮箱验证码逻辑 ===
////            // 生成验证码
////            String verificationCode = generateVerificationCode();
////
////            // 保存验证码到 Redis，有效期 10 分钟
////            redisTemplate.opsForValue().set("email:code:" + user.getEmail(), verificationCode, Duration.ofMinutes(10));
////
////            // 发送验证码邮件
////            sendEmailVerificationCode(user.getEmail(), verificationCode);
//
//            return savedUser;
//        } catch (Exception e) {
//            System.out.println("Error during user registration: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//
//    }

//    // 用户登录
//    public Optional<User> loginUser(String userID, String password, int roleId) {
//        Optional<User> user = userRepository.findByUserID(userID);
////        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
////            // 检查用户是否有该角色
////            if (user.get().getRoles().contains(roleId)) {
////                return user;
////            }
////        }
////        return Optional.empty();  // 登录失败
//        if (user.isPresent()) {
//            System.out.println("User found: " + user.get().getUserID());
//            System.out.println("Password matches: " + passwordEncoder.matches(password, user.get().getPassword()));
//            if (passwordEncoder.matches(password, user.get().getPassword())) {
//                return user;
//            }
//        }
//        return Optional.empty();
//    }



//    //实现修改用户信息
//    public User updateUserInfo(String userID, int roleId, User updatedUser) {
//        Optional<User> existingUserOpt = userRepository.findByUserID(userID);
//
//        if (existingUserOpt.isPresent()) {
//            User existingUser = existingUserOpt.get();
//
//            // 确保用户有该角色
//            List<UserRole> roles = userRoleRepository.findById_UserID(userID);
//            boolean hasRole = roles.stream().anyMatch(r -> r.getRoleID() == roleId);
//
//            if (!hasRole) {
//                throw new RuntimeException("Unauthorized to update this user.");
//            }
//
//            // 只允许修改除 userID 和角色外的字段
//            // 这里会对传入的字段进行判断，如果字段有修改就更新
//            if (updatedUser.getUsername() != null) {
//                existingUser.setUsername(updatedUser.getUsername());
//            }
//            if (updatedUser.getPhone() != null) {
//                existingUser.setPhone(updatedUser.getPhone());
//            }
//            if (updatedUser.getAge() != 0) {  // 你可以根据实际业务规则来判断age
//                existingUser.setAge(updatedUser.getAge());
//            }
//            if (updatedUser.getGender() != null) {
//                existingUser.setGender(updatedUser.getGender());
//            }
//            if (updatedUser.getLabel() != null) {
//                existingUser.setLabel(updatedUser.getLabel());
//            }
//
//            // 这里密码不能更新，如果用户提供了密码，则不做任何操作
//            if (updatedUser.getPassword() != null) {
//                throw new RuntimeException("Password cannot be updated.");
//            }
//
//            return userRepository.save(existingUser);
//        } else {
//            throw new RuntimeException("User not found.");
//        }
//    }
//    public void sendEmailVerificationCode(String email, String code) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Gym App Email Verification");
//        message.setText("Your verification code is: " + code + "\nThis code is valid for 10 minutes.");
//        mailSender.send(message);
//    }


