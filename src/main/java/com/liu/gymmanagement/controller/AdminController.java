package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.dto.GymTimeslotDTO;
import com.liu.gymmanagement.dto.ReservationDTO;
import com.liu.gymmanagement.model.Equipment;
import com.liu.gymmanagement.model.Gym;
import com.liu.gymmanagement.model.GymTimeslotTemplate;
import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.service.*;
import com.liu.gymmanagement.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private GymService gymService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private GymTimeslotService gymTimeslotService;
    @Autowired
    private GymTimeslotTemplateService gymTimeslotTemplateService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EquipmentService equipmentService;


    // 发送验证码接口
    @Autowired
    private EmailCodeCache localCache;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendEmailCode(@RequestParam String email) {
        if (!email.endsWith("@bupt.edu.cn")) {
            return ResponseEntity.badRequest().body("Only BUPT emails are allowed");
        }

        SecureRandom secureRandom = new SecureRandom();
        int code = secureRandom.nextInt(999999);
        String formattedCode = String.format("%06d", code);

        // ✅ 只用本地缓存保存验证码
        localCache.put(email, formattedCode, Duration.ofMinutes(10));

        try {
            userService.sendEmailVerificationCode(email, formattedCode);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification code due to an error.");
        }

        return ResponseEntity.ok("Verification code sent!");
    }


    // 管理员注册
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody User user, @RequestParam String verificationCode) {
        try {
            // 调用业务层注册方法
            User registeredUser = userService.registerUser(user, 2, verificationCode);  // 角色ID = 2
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            // 异常处理，返回具体的错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }


//    //发送验证码接口
//    @GetMapping("/send-code")
//    public ResponseEntity<?> sendEmailCode(@RequestParam String email) {
//        if (!email.endsWith("@bupt.edu.cn")) {
//            return ResponseEntity.badRequest().body("Only BUPT emails are allowed");
//        }
//
//        String code = String.format("%06d", new Random().nextInt(999999));
//        redisTemplate.opsForValue().set("email:code:" + email, code, Duration.ofMinutes(10));
//        userService.sendEmailVerificationCode(email, code);
//
//        return ResponseEntity.ok("Verification code sent!");
//    }
//
//    // 管理员注册 (不需要认证)
//    @PostMapping("/register")
//    public ResponseEntity<?> registerAdmin(@RequestBody User user, @RequestParam String verificationCode) {
//        User registeredUser = userService.registerUser(user, 2, verificationCode);  // 角色ID = 2 (管理员)
//
//        // 返回 ResponseEntity，包含注册的用户信息和 CREATED 状态码
//        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);}

    // 管理员登录 (不需要认证)
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody User loginUser) {
        return loginUser(loginUser, 2);  // 角色ID = 2 (管理员)
    }

    // 获取个人信息 (需要管理员认证)
    @GetMapping("/profile")
    public ResponseEntity<User> getAdminProfile(HttpServletRequest request) {
        String userId = jwtUtil.getUserIdFromRequest(request);
        Optional<User> user = userService.getUserInfo(userId, 2);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 修改个人信息 (需要管理员认证)
    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(HttpServletRequest request, @RequestBody User updatedUser) {
        try {
            String userId = jwtUtil.getUserIdFromRequest(request);
            User user = userService.updateUserInfo(userId, 2, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 查询所有健身房 (需要管理员认证)
    @GetMapping("/gyms")
    public ResponseEntity<List<Gym>> getAllGyms() {
        List<Gym> gyms = gymService.getAllGyms();
        if (gyms.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gyms);
    }

    // 更新健身房信息 (需要管理员认证)
    @PutMapping("/gyms/{gymId}")
    public ResponseEntity<String> updateGym(@PathVariable Integer gymId, @RequestBody Gym gym) {
        gym.setGymid(gymId);
        boolean isUpdated = gymService.updateGym(gym);
        if (isUpdated) {
            return ResponseEntity.ok("Gym updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gym not found or update failed");
        }
    }

    // 添加健身设备
    @PostMapping("equipment/add/{gymId}")
    public ResponseEntity<String> addEquipment(@PathVariable Integer gymId, @RequestBody Equipment equipment) {
        equipment.setGymid(gymId);
        equipmentService.addEquipment(equipment);
        return ResponseEntity.ok("添加成功");
    }

    // 修改健身设备
    @PutMapping("equipment/update")
    public ResponseEntity<String> updateEquipment(@RequestBody Equipment equipment) {
        equipmentService.updateEquipment(equipment);
        return ResponseEntity.ok("修改成功");
    }


    // 获取所有时段模板 (需要管理员认证)
    @GetMapping("/timeslot-templates")
    public List<GymTimeslotTemplate> getTimeslotTemplates() {
        return gymTimeslotTemplateService.getAllTemplates();
    }

    // 新增模板时段 (需要管理员认证)
    @PostMapping("/timeslot-templates")
    public ResponseEntity<String> addTimeslotTemplate(@RequestBody GymTimeslotTemplate template) {
        boolean success = gymTimeslotTemplateService.addTemplate(template);
        return success ? ResponseEntity.ok("模板添加成功") : ResponseEntity.badRequest().body("添加失败");
    }

    // 修改模板 (需要管理员认证)
    @PutMapping("/timeslot-templates/{id}")
    public ResponseEntity<String> updateTimeslotTemplate(@PathVariable int id, @RequestBody GymTimeslotTemplate template) {
        boolean success = gymTimeslotTemplateService.updateTemplate(id, template);
        return success ? ResponseEntity.ok("修改成功") : ResponseEntity.badRequest().body("修改失败");
    }

    // 删除模板 (需要管理员认证)
    @DeleteMapping("/timeslot-templates/{id}")
    public ResponseEntity<String> deleteTimeslotTemplate(@PathVariable int id) {
        boolean success = gymTimeslotTemplateService.deleteTemplate(id);
        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.badRequest().body("删除失败");
    }

    // 管理员查看所有预约时段的情况 (需要管理员认证)
    @GetMapping("/gyms/{gymId}/timeslots")
    public List<GymTimeslotDTO> getGymTimeslots(@PathVariable int gymId) {
        return gymTimeslotService.getTimeslotsForGym(gymId);
    }

    // 管理员修改预约时段的最大容量 (需要管理员认证)
    @PutMapping("/gyms/{gymId}/timeslots/{timeslotId}")
    public ResponseEntity<String> updateTimeslotCapacity(
            @PathVariable int gymId,
            @PathVariable int timeslotId,
            @RequestParam int newCapacity) {
        boolean success = gymTimeslotService.updateTimeslotCapacity(gymId, timeslotId, newCapacity);
        if (success) {
            return ResponseEntity.ok("时段容量更新成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("时段容量更新失败");
        }
    }

    // 管理员查看所有学生预约情况 (需要管理员认证)
    @GetMapping("/reservations")
    public List<ReservationDTO> getAllReservations() {
        return reservationService.getAllReservations();
    }

    // 管理员取消某个学生的预约 (需要管理员认证)
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<String> cancelStudentReservation(@PathVariable int reservationId) {
        boolean success = reservationService.cancelReservation(reservationId);
        if (success) {
            return ResponseEntity.ok("学生预约已取消");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("无法取消该预约");
        }
    }
    // 注册用户（公用方法）
    // 注册用户（公用方法）
    private ResponseEntity<?> registerUser(User user, int roleId, String verificationCode) {
        try {
            // 校验必要字段
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
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>("Email cannot be empty", HttpStatus.BAD_REQUEST);
            }

            // 调用 service 层的 registerUser 方法，加入验证码验证
            User registeredUser = userService.registerUser(user, roleId, verificationCode);

            // 如果注册成功，返回注册的用户信息
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            // 捕获异常，返回错误信息
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



//    // 注册用户（公用方法）
//    private ResponseEntity<?> registerUser(User user, int roleId) {
//        try {
//            if (user.getUserID() == null || user.getUserID().trim().isEmpty()) {
//                return new ResponseEntity<>("UserID cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
//                return new ResponseEntity<>("Username cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
//                return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
//                return new ResponseEntity<>("Phone cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//
//            User registeredUser = userService.registerUser(user, roleId);
//            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
//        } catch (RuntimeException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

    // 登录用户（公用方法）
    private ResponseEntity<?> loginUser(User loginUser, int roleId) {
        Optional<User> user = userService.loginUser(loginUser.getUserID(), loginUser.getPassword(), roleId);
        if (user.isPresent()) {
            String token = jwtUtil.generateToken(user.get().getUserID(), roleId);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.get().getUserID());
            response.put("roleId", String.valueOf(roleId));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}

//@RestController
//@RequestMapping("/api/admin")
//public class AdminController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private GymService gymService;
//
//    @Autowired
//    private ReservationService reservationService;
//
//    @Autowired
//    private GymTimeslotService gymTimeslotService;
//
//    @Autowired
//    private GymTimeslotTemplateService gymTimeslotTemplateService;
//
//    // 管理员注册
//    @PostMapping("/register")
//    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
//        return registerUser(user, 2);  // 角色ID = 2 (管理员)
//    }
//
//    // 管理员登录
//    @PostMapping("/login")
//    public ResponseEntity<String> loginAdmin(@RequestBody User loginUser) {
//        return loginUser(loginUser, 2);  // 角色ID = 2 (管理员)
//    }
//
//    // 获取个人信息
//    @GetMapping("/profile/{userID}")
//    public ResponseEntity<User> getAdminProfile(@PathVariable String userID) {
//        Optional<User> user = userService.getUserInfo(userID, 2); // 管理员角色 ID = 2
//        return user.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//    // 修改个人信息
//    @PutMapping("/profile/{userID}")
//    public ResponseEntity<?> updateAdminProfile(@PathVariable String userID, @RequestBody User updatedUser) {
//        try {
//            User user = userService.updateUserInfo(userID, 2, updatedUser);
//            return ResponseEntity.ok(user);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        }
//    }
//
//    // 查询所有健身房
//    @GetMapping("/gyms")
//    public ResponseEntity<List<Gym>> getAllGyms() {
//        List<Gym> gyms = gymService.getAllGyms();  // 查询所有健身房
//        if (gyms.isEmpty()) {
//            return ResponseEntity.noContent().build();  // 如果没有健身房数据，返回204 No Content
//        }
//        return ResponseEntity.ok(gyms);  // 返回200 OK和健身房数据
//    }
//
//    // 更新健身房信息
//    @PutMapping("/gyms/{gymId}")
//    public ResponseEntity<String> updateGym(@PathVariable Integer gymId, @RequestBody Gym gym) {
//        gym.setGymid(gymId); // Ensure the gym ID matches the one to be updated
//
//        boolean isUpdated = gymService.updateGym(gym);
//        if (isUpdated) {
//            return ResponseEntity.ok("Gym updated successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gym not found or update failed");
//        }
//    }
//
//    // 获取所有时段模板
//    @GetMapping("/timeslot-templates")
//    public List<GymTimeslotTemplate> getTimeslotTemplates() {
//        return gymTimeslotTemplateService.getAllTemplates();
//    }
//
//    // 新增模板时段
//    @PostMapping("/timeslot-templates")
//    public ResponseEntity<String> addTimeslotTemplate(@RequestBody GymTimeslotTemplate template) {
//        boolean success = gymTimeslotTemplateService.addTemplate(template);
//        return success ? ResponseEntity.ok("模板添加成功") : ResponseEntity.badRequest().body("添加失败");
//    }
//
//    // 修改模板
//    @PutMapping("/timeslot-templates/{id}")
//    public ResponseEntity<String> updateTimeslotTemplate(@PathVariable int id, @RequestBody GymTimeslotTemplate template) {
//        boolean success = gymTimeslotTemplateService.updateTemplate(id, template);
//        return success ? ResponseEntity.ok("修改成功") : ResponseEntity.badRequest().body("修改失败");
//    }
//
//    // 删除模板
//    @DeleteMapping("/timeslot-templates/{id}")
//    public ResponseEntity<String> deleteTimeslotTemplate(@PathVariable int id) {
//        boolean success = gymTimeslotTemplateService.deleteTemplate(id);
//        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.badRequest().body("删除失败");
//    }
//
//    // 管理员查看所有预约时段的情况
//    @GetMapping("/gyms/{gymId}/timeslots")
//    public List<GymTimeslotDTO> getGymTimeslots(@PathVariable int gymId) {
//        return gymTimeslotService.getTimeslotsForGym(gymId);
//    }
//
//    // 管理员修改预约时段的最大容量
//    @PutMapping("/gyms/{gymId}/timeslots/{timeslotId}")
//    public ResponseEntity<String> updateTimeslotCapacity(
//            @PathVariable int gymId,
//            @PathVariable int timeslotId,
//            @RequestParam int newCapacity) {
//        boolean success = gymTimeslotService.updateTimeslotCapacity(gymId, timeslotId, newCapacity);
//        if (success) {
//            return ResponseEntity.ok("时段容量更新成功");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("时段容量更新失败");
//        }
//    }
//
//    // 管理员查看所有学生预约情况
//    @GetMapping("/reservations")
//    public List<ReservationDTO> getAllReservations() {
//        return reservationService.getAllReservations();
//    }
//
//    // 管理员取消某个学生的预约
//    @DeleteMapping("/reservations/{reservationId}")
//    public ResponseEntity<String> cancelStudentReservation(@PathVariable int reservationId) {
//        boolean success = reservationService.cancelReservation(reservationId);
//        if (success) {
//            return ResponseEntity.ok("学生预约已取消");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("无法取消该预约");
//        }
//    }
//
//
//    // 注册用户（公用方法）
//    private ResponseEntity<?> registerUser(User user, int roleId) {
//        try {
//            // 打印接收到的用户数据，用于调试
//            System.out.println("Received user data: " + user);
//
//            // 基本验证
//            if (user.getUserID() == null || user.getUserID().trim().isEmpty()) {
//                return new ResponseEntity<>("UserID cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
//                return new ResponseEntity<>("Username cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
//                return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
//                return new ResponseEntity<>("Phone cannot be empty", HttpStatus.BAD_REQUEST);
//            }
//
//            User registeredUser = userService.registerUser(user,roleId);
//            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
//        } catch (RuntimeException e) {
//            // 打印详细错误信息到控制台
//            e.printStackTrace();
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    // 登录用户（公用方法）
//    private ResponseEntity<String> loginUser(User loginUser, int roleId) {
//        Optional<User> user = userService.loginUser(loginUser.getUserID(), loginUser.getPassword(), roleId);
//        if (user.isPresent()) {
//            return new ResponseEntity<>("Login successful", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
//        }
//    }
//}
