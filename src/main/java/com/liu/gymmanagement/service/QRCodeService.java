package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.AccesslogsMapper;  // 引入 MyBatis Mapper
import com.liu.gymmanagement.mapper.CapacityLogMapper;  // 引入 CapacityLogsMapper
import com.liu.gymmanagement.mapper.ReservationMapper;  // 引入 ReservationMapper
import com.liu.gymmanagement.model.Accesslogs;
import com.liu.gymmanagement.model.CapacityLog;
import com.liu.gymmanagement.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QRCodeService {

    @Autowired
    private ReservationMapper reservationMapper;  // 注入 ReservationMapper

    @Autowired
    private AccesslogsMapper accessLogMapper;  // 注入 AccessLogMapper

    @Autowired
    private CapacityLogMapper capacityLogsMapper;  // 注入 CapacityLogsMapper

    public ResponseEntity<String> handleQRCodeScan(String qrCodeData) {
        // 1️⃣ 查找预约信息
        Optional<Reservation> reservationOpt = reservationMapper.findByEntryQrCodeOrExitQrCode(qrCodeData);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            LocalDateTime now = LocalDateTime.now();
            // 2️⃣ 查找最近的一条入场记录
            Accesslogs accessLog = accessLogMapper.findLatestByUserIdAndGymId(reservation.getUserid(), reservation.getGymid());

            // 3️⃣ 处理入场逻辑
            if (qrCodeData.equals(reservation.getEntryQrCode())) {
                if (accessLog == null || accessLog.getExittime() != null) {
                    // 添加新的入场记录
                    Accesslogs newLog = new Accesslogs(reservation.getUserid(), reservation.getGymid(), now, null);
                    accessLogMapper.insertAccessLog(newLog);

                    // 🚀 更新 capacity_logs，增加1
                    capacityLogsMapper.incrementCurrentCount(reservation.getGymid());

                    return ResponseEntity.ok("✅ 入场成功");
                } else {
                    return ResponseEntity.badRequest().body("⚠️ 重复扫码，入场失败");
                }
            }
            // 4️⃣ 处理出场逻辑
            else if (qrCodeData.equals(reservation.getExitQrCode())) {
                if (accessLog != null && accessLog.getExittime() == null) {
                    accessLog.setExittime(now);
                    accessLogMapper.updateExitTime(reservation.getUserid(), reservation.getGymid(), now);

                    // 🚀 更新 capacity_logs，减少1
                    capacityLogsMapper.decrementCurrentCount(reservation.getGymid());

                    return ResponseEntity.ok("✅ 出场成功");
                } else {
                    return ResponseEntity.badRequest().body("⚠️ 未检测到入场记录，出场失败");
                }
            }
        }
        return ResponseEntity.badRequest().body("❌ 无效二维码");
    }
}
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
