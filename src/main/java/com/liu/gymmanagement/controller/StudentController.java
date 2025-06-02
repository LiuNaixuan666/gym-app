package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.dto.*;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.model.*;
import com.liu.gymmanagement.service.*;
import com.liu.gymmanagement.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GymService gymService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private WorkoutLogService workoutLogService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private GymTimeslotService gymTimeslotService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private QRCodeService qrCodeService;


//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

    // 假设 Map 用来做降级处理
    //private Map<String, String> localCache = new ConcurrentHashMap<>();

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

    // 学生注册
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody User user, @RequestParam String verificationCode) {
        try {
            // 调用业务层注册方法
            User registeredUser = userService.registerUser(user, 1, verificationCode);  // 角色ID = 1 (学生)
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            // 异常处理，返回具体的错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    // 学生登录 (不需要认证)
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody User loginUser) {
        return loginUser(loginUser, 1);  // 角色ID = 1 (学生)
    }

    // 查询用户信息 (需要学生认证)
    @GetMapping("/profile")
    public ResponseEntity<User> getStudentProfile(HttpServletRequest request) {
        String userId = jwtUtil.getUserIdFromRequest(request);
        Optional<User> user = userService.getUserInfo(userId, 1);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 修改用户信息 (需要学生认证)
    @PutMapping("/profile")
    public ResponseEntity<?> updateStudentProfile(HttpServletRequest request, @RequestBody User updatedUser) {
        try {
            String userId = jwtUtil.getUserIdFromRequest(request);
            User user = userService.updateUserInfo(userId, 1, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 查询所有健身房 (需要学生认证)
    @GetMapping("/gyms")
    public ResponseEntity<List<Gym>> getAllGyms() {
        List<Gym> gyms = gymService.getAllGyms();
        if (gyms.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gyms);
    }

    // 获取所有健身房的实时容量
    @GetMapping("/gym/capacity")
    public ResponseEntity<List<CapacityLogDTO>> getAllGymCurrentCapacity() {
        List<CapacityLogDTO> capacityLogs = gymService.getAllGymRealTimeCapacity();
        return ResponseEntity.ok(capacityLogs);
    }

    //获取健身房设备状态
    @GetMapping("/equipment/{gymId}")
    public List<EquipmentDTO> getEquipmentByGym(@PathVariable Integer gymId) {
        return equipmentService.getEquipmentByGymId(gymId);
    }

    // 查询未来7天的时段 (需要学生认证)
//    @GetMapping("/timeslots")
//    public List<GymTimeslot> getAvailableTimeslots() {
//        return gymTimeslotService.getFutureTimeslots();
//    }

    // 查询未来7天的时段 (需要学生认证)
    @GetMapping("/timeslots/{gymId}/{date}")
    public List<GymTimeslot> getAvailableTimeslots(
            @PathVariable int gymId,
            @PathVariable String date) {
        return gymTimeslotService.getTimeslotsForGym(gymId, date);
    }

    // 学生预约健身房 (需要学生认证)
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveGym(HttpServletRequest request,
                                             @RequestBody ReservationRequest reservationRequest) {
        String studentId = jwtUtil.getUserIdFromRequest(request);
        reservationRequest.setUserId(studentId);

        boolean success = reservationService.reserveGym(reservationRequest);
        if (success) {
            return ResponseEntity.ok("Appointment success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation failed, it may be full.");
        }
    }

    // 获取学生所有预约 (需要学生认证)
    @GetMapping("/reservations")
    public List<ReservationDTO> getStudentReservations(HttpServletRequest request) {
//        String userId = jwtUtil.getUserIdFromRequest(request);
//        return reservationService.getUserReservations(userId);
        String userId = jwtUtil.getUserIdFromRequest(request);
        // 添加排序参数，按预约时间降序排列
        return reservationService.getUserReservations(userId)
                .stream()
                .sorted(Comparator.comparing(ReservationDTO::getReservationTime).reversed())
                .collect(Collectors.toList());
    }

    // 处理树莓派传来的二维码数据（JSON 格式）
    @PostMapping("/reservations/scan")
    public ResponseEntity<Map<String, Object>> getScanFeedback(
            @RequestParam String userId,
            @RequestParam String type) {

        ScanFeedback feedback = qrCodeService.getFeedback(userId, type);
        if (feedback == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "No scan feedback available"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", feedback.isSuccess(),
                "message", feedback.getMessage(),
                "time", feedback.getTime().toString()
        ));
    }
//    public ResponseEntity<Map<String, Object>> processQRCodeScan1(@RequestBody Map<String, String> payload) {
//        String qrCodeData = payload.get("qrCodeData");
//        return qrCodeService.handleQRCodeScan1(qrCodeData);
//    }



    // 学生取消预约 (需要学生认证)
    @DeleteMapping("/reservations/cancel/{reservationId}")
    public ResponseEntity<String> cancelReservation(HttpServletRequest request,
                                                    @PathVariable int reservationId) {
        String studentId = jwtUtil.getUserIdFromRequest(request);

        if (!reservationService.isReservationBelongsToStudent(reservationId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot cancel other people's reservation.");
        }

        boolean canCancel = reservationService.isCancellable(reservationId);
        if (!canCancel) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot cancel, the cancellation time may have expired.");
        }

        boolean success = reservationService.cancelReservation(reservationId);
        if (success) {
            return ResponseEntity.ok("Cancel successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cancellation failed, and the cancellation time limit may have passed.");
        }

    }

    // 学生添加锻炼记录 (需要学生认证)
    @PostMapping("/workout")
    public ResponseEntity<String> addWorkout(@RequestBody WorkoutLog log, HttpServletRequest request) {
        // 从请求中获取 userId
        String userId = jwtUtil.getUserIdFromRequest(request);  // 使用现有的工具方法获取 userId

        // 设置到 WorkoutLog 对象中
        log.setUserid(userId);

        // 校验必填字段
        if (log.getActivitytype() == null || log.getDurationminutes() == null || log.getDate() == null) {
            return ResponseEntity.badRequest().body("Please fill in the complete exercise record information.");
        }

        // 添加锻炼记录
        workoutLogService.addWorkout(log);
        return ResponseEntity.ok("Exercise record added successfully.");
    }

    // 查询用户锻炼记录
    @GetMapping("/workout/{userId}")
    public List<WorkoutLog> getWorkoutLog(@PathVariable String userId) {
        return workoutLogService.getWorkoutsByUserId(userId);
    }

    //用户锻炼建议
    @GetMapping("/workout/suggestion")
    public ResponseEntity<String> getWorkoutSuggestion(HttpServletRequest request) {
        String userId = jwtUtil.getUserIdFromRequest(request);
        String suggestion = workoutLogService.generateWorkoutSuggestion(userId);
        return ResponseEntity.ok(suggestion);
    }
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, String>> submitFeedback(@RequestBody FeedbackDTO dto, HttpServletRequest request) {
        // 从 JWT token 中提取 userId
        String userId = jwtUtil.getUserIdFromRequest(request);
        dto.setUserId(userId);  // 设置到 DTO 中

        // 校验字段
        if (dto.getContent() == null || dto.getCategory() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Feedback content or type cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        // 提交反馈
        feedbackService.submitFeedback(dto);

        // 返回 JSON 格式响应
        Map<String, String> response = new HashMap<>();
        response.put("message", "Feedback has been submitted, thank you for your comments.！");
        return ResponseEntity.ok(response);
    }


    //通知查询
    @GetMapping("/notifications")
    public List<NotificationDTO> getMyNotifications(HttpServletRequest request) {
        String userId = jwtUtil.getUserIdFromRequest(request); // 使用你之前写好的 JWT 工具类
        return notificationService.getNotificationsForUser(userId);
    }



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
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

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
// 发送验证码接口
//    @PostMapping("/send-code")
//    public ResponseEntity<?> sendEmailCode(@RequestParam String email) {
//        if (!email.endsWith("@bupt.edu.cn")) {
//            return ResponseEntity.badRequest().body("Only BUPT emails are allowed");
//        }
//
//        // 使用 SecureRandom 来生成验证码
//        SecureRandom secureRandom = new SecureRandom();
//        int code = secureRandom.nextInt(999999);  // 生成一个随机数
//        String formattedCode = String.format("%06d", code); // 保证6位数
//
//        // 保存验证码到 Redis，有效期 10 分钟
//        // 尝试使用 Redis 存储验证码
//        try {
//            redisTemplate.opsForValue().set("email:code:" + email, formattedCode, Duration.ofMinutes(10));
//        } catch (Exception e) {
//            // Redis 连接失败，使用内存缓存作为备选方案
//            localCache.put(email, formattedCode);
//            logger.error("Redis connection failed for saving email code for {}: {}", email, e.getMessage());
//        }
//        try {
//            // 发送验证码邮件
//            userService.sendEmailVerificationCode(email, formattedCode);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to send verification code due to an error.");
//        }
//
//        return ResponseEntity.ok("Verification code sent!");
//    }
//
//

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





//    //查询用户信息
//    @GetMapping("/profile/{userID}")
//    public ResponseEntity<User> getStudentProfile(@PathVariable String userID) {
//        Optional<User> user = userService.getUserInfo(userID, 1); // 学生角色 ID = 1
//        return user.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//
//    //修改用户信息
//    @PutMapping("/profile/{userID}")
//    public ResponseEntity<?> updateStudentProfile(@PathVariable String userID, @RequestBody User updatedUser) {
//        try {
//            User user = userService.updateUserInfo(userID, 1, updatedUser);
//            return ResponseEntity.ok(user);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        }
//    }
//
//    /// 查询所有健身房
//    @GetMapping("/gyms")
//    public ResponseEntity<List<Gym>> getAllGyms() {
//        List<Gym> gyms = gymService.getAllGyms();  // 查询所有健身房
//        if (gyms.isEmpty()) {
//            return ResponseEntity.noContent().build();  // 如果没有健身房数据，返回204 No Content
//        }
//        return ResponseEntity.ok(gyms);  // 返回200 OK和健身房数据
//    }
//
//    // 查询未来 7 天的时段
//    @GetMapping("/timeslots")
//    public List<GymTimeslot> getAvailableTimeslots() {
//        return gymTimeslotService.getFutureTimeslots();
//    }
//
////    // 学生预约健身房
////    @PostMapping("/reserve")
////    public ResponseEntity<String> reserveGym(@RequestBody ReservationRequest reservationRequest) {
////        boolean success = reservationService.reserveGym(reservationRequest);
////        if (success) {
////            return ResponseEntity.ok("预约成功");
////        } else {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("预约失败，可能已约满");
////        }
////    }
//
//    // 学生预约健身房
//    @PostMapping("/reserve")
//    public ResponseEntity<String> reserveGym(HttpServletRequest request,
//                                             @RequestBody ReservationRequest reservationRequest) {
//        // 从 Token 解析当前学生 ID
//        String studentId = jwtUtil.getStudentIdFromRequest(request);
//        reservationRequest.setUserId(studentId); // 强制绑定当前用户
//
//        boolean success = reservationService.reserveGym(reservationRequest);
//        if (success) {
//            return ResponseEntity.ok("预约成功");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("预约失败，可能已约满");
//        }
//    }
//
//
//    // 获取学生所有预约
//    @GetMapping("/reservations")
//    public List<ReservationDTO> getStudentReservations(@RequestParam String userId) {
//        return reservationService.getUserReservations(userId);
//    }
//
//    // 学生取消预约
//    @DeleteMapping("/cancel/{reservationId}")
//    public ResponseEntity<String> cancelReservation(HttpServletRequest request,
//                                                    @PathVariable int reservationId) {
//        String studentId = jwtUtil.getStudentIdFromRequest(request); // 获取当前登录的学生 ID
//
//        // 确保该预约属于当前学生，防止越权取消别人的预约
//        if (!reservationService.isReservationBelongsToStudent(reservationId, studentId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法取消他人预约");
//        }
//
//        // 调用 Service 判断是否可取消
//        boolean canCancel = reservationService.isCancellable(reservationId);
//        if (!canCancel) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("无法取消，可能已过取消时限");
//        }
//
//        boolean success = reservationService.cancelReservation(reservationId);
//        if (success) {
//            return ResponseEntity.ok("取消成功");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("取消失败，可能已过取消时限");
//        }
//    }
//
//
////    // 学生取消预约
////    @DeleteMapping("/cancel/{reservationId}")
////    public ResponseEntity<String> cancelReservation(@PathVariable int reservationId) {
////        // 调用 Service 中的取消判断逻辑
////        boolean canCancel = reservationService.isCancellable(reservationId);
////
////        if (canCancel) {
////            boolean success = reservationService.cancelReservation(reservationId);
////            if (success) {
////                return ResponseEntity.ok("取消成功");
////            } else {
////                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("取消失败，可能已过取消时限");
////            }
////        } else {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("无法取消，可能已过取消时限");
////        }
////    }
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
//

//    // 添加锻炼记录
//    @PostMapping("/workout")
//    public ResponseEntity<String> addWorkout(@RequestBody WorkoutLog log) {
//        if (log.getUserid() == null || log.getActivitytype() == null ||
//                log.getDurationminutes() == null || log.getDate() == null) {
//            return ResponseEntity.badRequest().body("请填写完整的锻炼记录信息");
//        }
//        workoutLogService.addWorkout(log);
//        return ResponseEntity.ok("锻炼记录添加成功");
//    }



//    //用户反馈
//    @PostMapping("/feedback")
//    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackDTO dto, HttpServletRequest request) {
//        // 从 JWT token 中提取 userId
//        String userId = jwtUtil.getUserIdFromRequest(request);
//        dto.setUserId(userId);  // 设置到 DTO 中
//
//        // 校验字段
//        if (dto.getContent() == null || dto.getCategory() == null) {
//            return ResponseEntity.badRequest().body("反馈内容或类型不能为空");
//        }
//
//        // 提交反馈
//        feedbackService.submitFeedback(dto);
//        return ResponseEntity.ok("反馈已提交，谢谢你的意见！");
//    }
