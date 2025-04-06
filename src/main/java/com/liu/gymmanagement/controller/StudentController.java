package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.dto.CapacityLogDTO;
import com.liu.gymmanagement.dto.ReservationDTO;
import com.liu.gymmanagement.dto.ReservationRequest;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.model.CapacityLog;
import com.liu.gymmanagement.model.Gym;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.User;
import com.liu.gymmanagement.service.GymService;
import com.liu.gymmanagement.service.GymTimeslotService;
import com.liu.gymmanagement.service.ReservationService;
import com.liu.gymmanagement.service.UserService;
import com.liu.gymmanagement.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private GymService gymService;

    @Autowired
    private ReservationService reservationService;

//    @Autowired
//    private GymTimeslotMapper gymTimeslotMapper;

    @Autowired
    private GymTimeslotService gymTimeslotService;

    @Autowired
    private JwtUtil jwtUtil;

    // 学生注册
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody User user) {
        return registerUser(user, 1);  // 角色ID = 1 (学生)
    }

    // 学生登录 (不需要认证)
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody User loginUser) {
        return loginUser(loginUser, 1);  // 角色ID = 1 (学生)
    }

//    // 学生登录
//    @PostMapping("/login")
//    public ResponseEntity<String> loginStudent(@RequestBody User loginUser) {
//        return loginUser(loginUser, 1);  // 角色ID = 1 (学生)
//    }

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

    // 查询未来7天的时段 (需要学生认证)
//    @GetMapping("/timeslots")
//    public List<GymTimeslot> getAvailableTimeslots() {
//        return gymTimeslotService.getFutureTimeslots();
//    }

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
            return ResponseEntity.ok("预约成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("预约失败，可能已约满");
        }
    }

    // 获取学生所有预约 (需要学生认证)
    @GetMapping("/reservations")
    public List<ReservationDTO> getStudentReservations(HttpServletRequest request) {
        String userId = jwtUtil.getUserIdFromRequest(request);
        return reservationService.getUserReservations(userId);
    }



    // 学生取消预约 (需要学生认证)
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<String> cancelReservation(HttpServletRequest request,
                                                    @PathVariable int reservationId) {
        String studentId = jwtUtil.getUserIdFromRequest(request);

        if (!reservationService.isReservationBelongsToStudent(reservationId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法取消他人预约");
        }

        boolean canCancel = reservationService.isCancellable(reservationId);
        if (!canCancel) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("无法取消，可能已过取消时限");
        }

        boolean success = reservationService.cancelReservation(reservationId);
        if (success) {
            return ResponseEntity.ok("取消成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("取消失败，可能已过取消时限");
        }
    }

    // 注册用户（公用方法）
    private ResponseEntity<?> registerUser(User user, int roleId) {
        try {
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

            User registeredUser = userService.registerUser(user, roleId);
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


}
