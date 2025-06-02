package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.AccesslogsCustomMapper;
import com.liu.gymmanagement.mapper.AccesslogsMapper;  // 引入 MyBatis Mapper
import com.liu.gymmanagement.mapper.CapacityLogMapper;  // 引入 CapacityLogsMapper
import com.liu.gymmanagement.mapper.ReservationMapper;  // 引入 ReservationMapper
import com.liu.gymmanagement.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QRCodeService {

    @Autowired
    private ReservationMapper reservationMapper;  // 注入 ReservationMapper

    @Autowired
    private AccesslogsCustomMapper accessLogMapper;  // 注入 AccessLogMapper

    @Autowired
    private CapacityLogMapper capacityLogsMapper;  // 注入 CapacityLogsMapper

    private final Map<String, ScanFeedback> feedbackCache = new ConcurrentHashMap<>();

    public ScanFeedback getFeedback(String userId, String type) {
        return feedbackCache.get(userId + "_" + type);
    }

    private void cacheFeedback(String userId, String type, boolean success, String message) {
        String key = userId + "_" + type;
        feedbackCache.put(key, new ScanFeedback(success, message, LocalDateTime.now()));
    }

    public ResponseEntity<Map<String, Object>> handleQRCodeScan(String qrCodeData) {
        String regex = "(\\d+)_(\\d+)_(entry|exit)_(\\d+)_([a-f0-9\\-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(qrCodeData);

        Map<String, Object> response = new HashMap<>();

        if (matcher.matches()) {
            int gymId = Integer.parseInt(matcher.group(1));
            String userId = matcher.group(2);
            String type = matcher.group(3);
            int reservationId = Integer.parseInt(matcher.group(4));
            String uniqueCode = matcher.group(5);

            ReservationExample example = new ReservationExample();
            ReservationExample.Criteria criteria = example.createCriteria();
            criteria.andReservationidEqualTo(reservationId);

            List<Reservation> reservations = reservationMapper.selectByExample(example);
            if (reservations.isEmpty()) {
                cacheFeedback(userId, type, false, "Reservation record not found");
                response.put("success", false);
                response.put("message", "Reservation record not found");
                return ResponseEntity.badRequest().body(response);
            }
            Reservation reservation = reservations.get(0);
            LocalDateTime now = LocalDateTime.now();

            Accesslogs accessLog = accessLogMapper.findLatestByUserIdGymIdAndReservationId(userId, gymId, reservationId);

            if (type.equals("entry")) {
                if (accessLog == null || accessLog.getExittime() != null) {
                    Accesslogs newAccessLog = new Accesslogs(userId, gymId, reservationId, now, null);
                    accessLogMapper.insertAccessLog(newAccessLog);
                    capacityLogsMapper.incrementCurrentCount(gymId);

                    cacheFeedback(userId, type, true, "✅ Entry successful");
                    response.put("success", true);
                    response.put("message", "✅ Entry successful");
                    return ResponseEntity.ok(response);
                } else {
                    cacheFeedback(userId, type, false, "⚠️ Already checked in, duplicate entry scan failed");
                    response.put("success", false);
                    response.put("message", "⚠️ Already checked in, duplicate entry scan failed");
                    return ResponseEntity.badRequest().body(response);
                }
            } else if (type.equals("exit")) {
                if (accessLog != null && accessLog.getExittime() == null) {
                    accessLog.setExittime(now);
                    accessLogMapper.updateExitTime(userId, gymId, reservationId, now);
                    capacityLogsMapper.decrementCurrentCount(gymId);

                    cacheFeedback(userId, type, true, "✅ Exit successful");
                    response.put("success", true);
                    response.put("message", "✅ Exit successful");
                    return ResponseEntity.ok(response);
                } else {
                    cacheFeedback(userId, type, false, "⚠️ No entry record detected, exit failed");
                    response.put("success", false);
                    response.put("message", "⚠️ No entry record detected, exit failed");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                cacheFeedback(userId, type, false, "❌ Invalid QR code type");
                response.put("success", false);
                response.put("message", "❌ Invalid QR code type");
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            cacheFeedback("unknown", "unknown", false, "❌ Invalid QR code format");
            response.put("success", false);
            response.put("message", "❌ Invalid QR code format");
            return ResponseEntity.badRequest().body(response);
        }
    }
}



//    public ResponseEntity<Map<String, Object>> handleQRCodeScan1(String qrCodeData) {
//        Map<String, Object> response = new HashMap<>();
//
//        String regex = "(\\d+)_(\\d+)_(entry|exit)_(\\d+)_([a-f0-9\\-]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(qrCodeData);
//
//        if (matcher.matches()) {
//            int gymId = Integer.parseInt(matcher.group(1));  // Gym ID
//            String userId = matcher.group(2);  // User ID
//            String type = matcher.group(3);  // "entry" 或 "exit"
//            int reservationId = Integer.parseInt(matcher.group(4));  // Reservation ID
//            String uniqueCode = matcher.group(5);  // 唯一的二维码标识符
//
//            // 查找预约记录
//            ReservationExample example = new ReservationExample();
//            ReservationExample.Criteria criteria = example.createCriteria();
//            criteria.andReservationidEqualTo(reservationId);
//            List<Reservation> reservations = reservationMapper.selectByExample(example);
//
//            if (reservations.isEmpty()) {
//                response.put("success", false);
//                response.put("message", "The appointment record could not be found");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            Reservation reservation = reservations.get(0);
//            LocalDateTime now = LocalDateTime.now();
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdGymIdAndReservationId(userId, gymId, reservationId);
//
//            // 入场处理
//            if (type.equals("entry")) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    Accesslogs newAccessLog = new Accesslogs(userId, gymId, reservationId, now, null);
//                    accessLogMapper.insertAccessLog(newAccessLog);
//                    capacityLogsMapper.incrementCurrentCount(gymId);
//
//                    response.put("success", true);
//                    response.put("message", "Admission was successful");
//                    return ResponseEntity.ok(response);
//                } else {
//                    response.put("success", false);
//                    response.put("message", "The user has already entered the market, repeatedly scanned the QR code, and the entry failed");
//                    return ResponseEntity.badRequest().body(response);
//                }
//            }
//
//            // 出场处理
//            else if (type.equals("exit")) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    accessLog.setExittime(now);
//                    accessLogMapper.updateExitTime(userId, gymId, reservationId, now);
//                    capacityLogsMapper.decrementCurrentCount(gymId);
//
//                    response.put("success", true);
//                    response.put("message", "The appearance was successful, welcome to come back next time!");
//                    return ResponseEntity.ok(response);
//                } else {
//                    response.put("success", false);
//                    response.put("message", "No entry record was detected, and the exit failed");
//                    return ResponseEntity.badRequest().body(response);
//                }
//            } else {
//                response.put("success", false);
//                response.put("message", "Invalid QR code type");
//                return ResponseEntity.badRequest().body(response);
//            }
//        } else {
//            response.put("success", false);
//            response.put("message", "Invalid QR code format");
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    public ResponseEntity<String> handleQRCodeScan(String qrCodeData) {
//        // 使用正则表达式拆分二维码数据
//        String regex = "(\\d+)_(\\d+)_(entry|exit)_(\\d+)_([a-f0-9\\-]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(qrCodeData);
//
//        if (matcher.matches()) {
//            int gymId = Integer.parseInt(matcher.group(1));  // Gym ID
//            String userId = matcher.group(2);  // User ID
//            String type = matcher.group(3);  // "entry" 或 "exit"
//            int reservationId = Integer.parseInt(matcher.group(4));  // Reservation ID
//            String uniqueCode = matcher.group(5);  // 唯一的二维码标识符
//
//            ReservationExample example = new ReservationExample();
//            ReservationExample.Criteria criteria = example.createCriteria();
//            criteria.andReservationidEqualTo(reservationId);
//
//            List<Reservation> reservations = reservationMapper.selectByExample(example);
//            if (reservations.isEmpty()) {
//                return ResponseEntity.badRequest().body("找不到该预约记录");
//            }
//            Reservation reservation = reservations.get(0);
//
//
//            LocalDateTime now = LocalDateTime.now();
//
//            // 2️⃣ 查找最近的入场记录
//            //Accesslogs accessLog = accessLogMapper.findLatestByUserIdAndGymId(userId, gymId);
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdGymIdAndReservationId(userId, gymId, reservationId);
//
//
//            // 3️⃣ 处理入场逻辑
//            if (type.equals("entry")) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    // 插入新的入场记录，包含 reservationId
//                    Accesslogs newAccessLog = new Accesslogs(userId, gymId, reservationId, now, null);
//                    accessLogMapper.insertAccessLog(newAccessLog);
//
//                    // 🚀 更新 capacity_logs，增加1
//                    capacityLogsMapper.incrementCurrentCount(gymId);
//
//                    return ResponseEntity.ok("✅ 入场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("⚠️ 该用户已经入场，重复扫码，入场失败");
//                }
//            }
//
//            // 4️⃣ 处理出场逻辑
//            else if (type.equals("exit")) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    // 更新出场时间
//                    accessLog.setExittime(now);
//                    //accessLogMapper.updateExitTime(userId, gymId, now);
//                    accessLogMapper.updateExitTime(userId, gymId, reservationId, now);
//
//                    // 🚀 更新 capacity_logs，减少1
//                    capacityLogsMapper.decrementCurrentCount(gymId);
//
//                    return ResponseEntity.ok("✅ 出场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("⚠️ 未检测到入场记录，出场失败");
//                }
//            } else {
//                return ResponseEntity.badRequest().body("❌ 无效二维码类型");
//            }
//        } else {
//            return ResponseEntity.badRequest().body("❌ 无效二维码格式");
//        }
//    }
//}
//
//    public ResponseEntity<Map<String, Object>> handleQRCodeScan(String qrCodeData) {
//        String regex = "(\\d+)_(\\d+)_(entry|exit)_(\\d+)_([a-f0-9\\-]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(qrCodeData);
//
//        Map<String, Object> response = new HashMap<>();
//
//        if (matcher.matches()) {
//            int gymId = Integer.parseInt(matcher.group(1));
//            String userId = matcher.group(2);
//            String type = matcher.group(3);
//            int reservationId = Integer.parseInt(matcher.group(4));
//            String uniqueCode = matcher.group(5);
//
//            ReservationExample example = new ReservationExample();
//            ReservationExample.Criteria criteria = example.createCriteria();
//            criteria.andReservationidEqualTo(reservationId);
//
//            List<Reservation> reservations = reservationMapper.selectByExample(example);
//            if (reservations.isEmpty()) {
//                response.put("success", false);
//                response.put("type", "error");
//                response.put("message", "Reservation not found.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            Reservation reservation = reservations.get(0);
//            LocalDateTime now = LocalDateTime.now();
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdGymIdAndReservationId(userId, gymId, reservationId);
//
//            if (type.equals("entry")) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    Accesslogs newAccessLog = new Accesslogs(userId, gymId, reservationId, now, null);
//                    accessLogMapper.insertAccessLog(newAccessLog);
//                    capacityLogsMapper.incrementCurrentCount(gymId);
//
//                    response.put("success", true);
//                    response.put("type", "entry");
//                    response.put("message", "Entry successful. Please proceed to the gym.");
//                    return ResponseEntity.ok(response);
//                } else {
//                    response.put("success", false);
//                    response.put("type", "error");
//                    response.put("message", "User has already entered. Entry denied.");
//                    return ResponseEntity.badRequest().body(response);
//                }
//            } else if (type.equals("exit")) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    accessLog.setExittime(now);
//                    accessLogMapper.updateExitTime(userId, gymId, reservationId, now);
//                    capacityLogsMapper.decrementCurrentCount(gymId);
//
//                    response.put("success", true);
//                    response.put("type", "exit");
//                    response.put("message", "Exit successful. See you next time!");
//                    return ResponseEntity.ok(response);
//                } else {
//                    response.put("success", false);
//                    response.put("type", "error");
//                    response.put("message", "No valid entry found. Exit denied.");
//                    return ResponseEntity.badRequest().body(response);
//                }
//            } else {
//                response.put("success", false);
//                response.put("type", "error");
//                response.put("message", "Invalid QR code type.");
//                return ResponseEntity.badRequest().body(response);
//            }
//        } else {
//            response.put("success", false);
//            response.put("type", "error");
//            response.put("message", "Invalid QR code format.");
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//}


//            // 1️⃣ 查找预约信息
//            Optional<Reservation> reservationOpt = reservationMapper.findById(reservationId);
//            if (!reservationOpt.isPresent()) {
//                return ResponseEntity.badRequest().body("❌ 找不到该预约记录");
//            }
//            Reservation reservation = reservationOpt.get();
//    public ResponseEntity<String> handleQRCodeScan(String qrCodeData) {
//        // 使用正则表达式拆分二维码数据
//        String regex = "(\\d+)-(\\d+)-(entry|exit)-([a-f0-9\\-]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(qrCodeData);
//
//        if (matcher.matches()) {
//            int gymId = Integer.parseInt(matcher.group(1));  // Gym ID
//            String userId = matcher.group(2);  // User ID
//            String type = matcher.group(3);  // "entry" 或 "exit"
//            String uniqueCode = matcher.group(4);  // 唯一的二维码标识符
//
//            // 1️⃣ 查找预约信息
//            Optional<Reservation> reservationOpt = reservationMapper.findByEntryQrCodeOrExitQrCode(qrCodeData);
//            if (!reservationOpt.isPresent()) {
//                return ResponseEntity.badRequest().body("找不到该二维码的预约记录");
//            }
//            Reservation reservation = reservationOpt.get();
//
//            LocalDateTime now = LocalDateTime.now();
//
//            // 2️⃣ 查找最近的入场记录
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdAndGymId(userId, gymId);
//
//            // 3️⃣ 处理入场逻辑
//            if (type.equals("entry")) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    // 插入新的入场记录
//                    Accesslogs newAccessLog = new Accesslogs(userId, gymId, now, null);
//                    // 使用 MyBatis 自动生成的 insert 方法
//                    accessLogMapper.insertSelective(newAccessLog);  // 插入新记录
//
//                    // 🚀 更新 capacity_logs，增加1
//                    capacityLogsMapper.incrementCurrentCount(gymId);
//
//                    return ResponseEntity.ok("入场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("该用户已经入场，重复扫码，入场失败");
//                }
//            }
//
//            // 4️⃣ 处理出场逻辑
//            else if (type.equals("exit")) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    // 更新出场时间
//                    accessLog.setExittime(now);
//                    accessLogMapper.updateByPrimaryKeySelective(accessLog);  // 更新出场时间
//
//                    // 🚀 更新 capacity_logs，减少1
//                    capacityLogsMapper.decrementCurrentCount(gymId);
//
//                    return ResponseEntity.ok("出场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("未检测到入场记录，出场失败");
//                }
//            } else {
//                return ResponseEntity.badRequest().body("无效二维码类型");
//            }
//        } else {
//            return ResponseEntity.badRequest().body("无效二维码格式");
//        }
//    }


//
//    public ResponseEntity<String> handleQRCodeScan(String qrCodeData) {
//        // 1️⃣ 查找预约信息
//        Optional<Reservation> reservationOpt = reservationMapper.findByEntryQrCodeOrExitQrCode(qrCodeData);
//
//        if (reservationOpt.isPresent()) {
//            Reservation reservation = reservationOpt.get();
//            LocalDateTime now = LocalDateTime.now();
//            // 2️⃣ 查找最近的一条入场记录
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdAndGymId(reservation.getUserid(), reservation.getGymid());
//
//            // 3️⃣ 处理入场逻辑
//            if (qrCodeData.equals(reservation.getEntryQrCode())) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    // 添加新的入场记录
//                    Accesslogs newLog = new Accesslogs(reservation.getUserid(), reservation.getGymid(), now, null);
//                    accessLogMapper.insertAccessLog(newLog);
//
//                    // 🚀 更新 capacity_logs，增加1
//                    capacityLogsMapper.incrementCurrentCount(reservation.getGymid());
//
//                    return ResponseEntity.ok("✅ 入场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("⚠️ 重复扫码，入场失败");
//                }
//            }
//            // 4️⃣ 处理出场逻辑
//            else if (qrCodeData.equals(reservation.getExitQrCode())) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    accessLog.setExittime(now);
//                    accessLogMapper.updateExitTime(reservation.getUserid(), reservation.getGymid(), now);
//
//                    // 🚀 更新 capacity_logs，减少1
//                    capacityLogsMapper.decrementCurrentCount(reservation.getGymid());
//
//                    return ResponseEntity.ok("✅ 出场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("⚠️ 未检测到入场记录，出场失败");
//                }
//            }
//        }
//        return ResponseEntity.badRequest().body("❌ 无效二维码");
//    }

//package com.liu.gymmanagement.service;
//
//import com.liu.gymmanagement.mapper.AccesslogsMapper;  // 引入 MyBatis Mapper
//import com.liu.gymmanagement.mapper.ReservationMapper;  // 引入 ReservationMapper
//import com.liu.gymmanagement.model.Accesslogs;
//import com.liu.gymmanagement.model.CapacityLog;
//import com.liu.gymmanagement.model.Reservation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//public class QRCodeService {
//
//    @Autowired
//    private ReservationMapper reservationMapper;  // 注入 MyBatis Mapper
//
//    @Autowired
//    private AccesslogsMapper accessLogMapper;  // 注入 AccessLogMapper
//
//
//    //private CapacityLog capacityLogsMapper;
//
//    public ResponseEntity<String> handleQRCodeScan(String qrCodeData) {
//        Optional<Reservation> reservationOpt = reservationMapper.findByEntryQrCodeOrExitQrCode(qrCodeData);  // 使用 MyBatis 查找预定记录
//
//        if (reservationOpt.isPresent()) {
//            Reservation reservation = reservationOpt.get();
//            LocalDateTime now = LocalDateTime.now();
//            Accesslogs accessLog = accessLogMapper.findLatestByUserIdAndGymId(reservation.getUserid(), reservation.getGymid());
//
//            if (qrCodeData.equals(reservation.getEntryQrCode())) {
//                if (accessLog == null || accessLog.getExittime() != null) {
//                    Accesslogs newLog = new Accesslogs(reservation.getUserid(), reservation.getGymid(), now, null);
//                    accessLogMapper.insertAccessLog(newLog);  // 使用 MyBatis 插入新的出入记录
//                    return ResponseEntity.ok("入场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("重复扫码，入场失败");
//                }
//            } else if (qrCodeData.equals(reservation.getExitQrCode())) {
//                if (accessLog != null && accessLog.getExittime() == null) {
//                    accessLog.setExittime(now);
//                    accessLogMapper.updateExitTime(reservation.getUserid(), reservation.getGymid(), now);  // 更新出场时间
//                    return ResponseEntity.ok("出场成功");
//                } else {
//                    return ResponseEntity.badRequest().body("未检测到入场记录，出场失败");
//                }
//            }
//        }
//        return ResponseEntity.badRequest().body("无效二维码");
//    }
//}
