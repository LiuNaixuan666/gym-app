package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.ReservationDTO;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.mapper.ReservationMapper;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.GymTimeslotExample;
import com.liu.gymmanagement.model.Reservation;
import com.liu.gymmanagement.dto.ReservationRequest;
import com.liu.gymmanagement.model.ReservationExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
//import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private GymTimeslotMapper gymTimeslotMapper;

    public String generateQrCode(String userId, int gymId, String type, int reservationId) {
        String uniqueCode = UUID.randomUUID().toString(); // 生成唯一标识
        return gymId + "_" + userId + "_" + type + "_" + reservationId + "_" + uniqueCode;
    }

    public boolean reserveGym(ReservationRequest request) {
        // 查询时段信息
        GymTimeslot timeslot = gymTimeslotMapper.selectByPrimaryKey(request.getTimeslotId());
        if (timeslot == null || timeslot.getCurrentReservations() >= timeslot.getMaxCapacity()) {
            return false; // 已满
        }

        // 插入预约记录
        Reservation reservation = new Reservation();
        reservation.setUserid(request.getUserId());
        reservation.setGymid(request.getGymId());
        reservation.setTimeslotid(request.getTimeslotId());
        reservation.setStatus("booked"); // 设置状态为 booked
        reservation.setReservationTime(LocalDateTime.now());

        // 先插入预约记录，获取自动生成的 reservationId
        reservationMapper.insertReservation(reservation);  // 假设 reservationMapper 配置了返回主键功能

        // 获取自动生成的 reservationId
        Integer reservationId = reservation.getReservationid(); // 注意这里使用 Integer 类型避免 NPE

        if (reservationId == null) {
            // 如果 reservationId 为 null，表示插入失败或未能获取到主键
            return false;
        }

//        // 生成二维码（将 reservationId 传入生成二维码方法）
//        reservation.setEntryQrCode(generateQrCode(request.getUserId(), request.getGymId(), "entry", reservationId));
//        reservation.setExitQrCode(generateQrCode(request.getUserId(), request.getGymId(), "exit", reservationId));
//
        // 生成二维码
        String entryCode = generateQrCode(request.getUserId(), request.getGymId(), "entry", reservationId);
        String exitCode = generateQrCode(request.getUserId(), request.getGymId(), "exit", reservationId);

        // 更新二维码字段
        reservation.setEntryQrCode(entryCode);
        reservation.setExitQrCode(exitCode);
        reservationMapper.updateByPrimaryKeySelective(reservation); // 更新二维码

        // 更新时段表的 current_reservations +1
        timeslot.setCurrentReservations(timeslot.getCurrentReservations() + 1);
        gymTimeslotMapper.updateByPrimaryKeySelective(timeslot);

        return true;
    }

    public boolean cancelReservation(int reservationId) {
        // 查询预约记录
        Reservation reservation = reservationMapper.selectByPrimaryKey(reservationId);
        if (reservation == null || !isCancellable(reservationId)) {
            return false;
        }

        // 修改预约状态为 cancelled
        reservation.setStatus("cancelled");
        reservationMapper.updateByPrimaryKeySelective(reservation);

        // 更新时段表的 current_reservations -1
        GymTimeslot timeslot = gymTimeslotMapper.selectByPrimaryKey(reservation.getTimeslotid());
        if (timeslot != null && timeslot.getCurrentReservations() > 0) {
            timeslot.setCurrentReservations(timeslot.getCurrentReservations() - 1);
            gymTimeslotMapper.updateByPrimaryKeySelective(timeslot);
        }

        return true;
    }

    // 检查预约是否属于当前学生
    public boolean isReservationBelongsToStudent(int reservationId, String studentId) {
        Reservation reservation = reservationMapper.selectByPrimaryKey(reservationId);
        return reservation != null && reservation.getUserid().equals(studentId);
    }

    public List<ReservationDTO> getUserReservations(String userId) {
        ReservationExample example = new ReservationExample();
        example.createCriteria().andUseridEqualTo(userId);
        List<Reservation> reservations = reservationMapper.selectByExample(example);

        // 转换为 DTO 对象
        List<ReservationDTO> reservationDTOList = new ArrayList<>();
        for (Reservation reservation : reservations) {
            ReservationDTO dto = new ReservationDTO();
            dto.setReservationId(reservation.getReservationid());
            dto.setGymId(reservation.getGymid());
            dto.setUserId(reservation.getUserid());
            dto.setTimeslotId(reservation.getTimeslotid());
            dto.setReservationTime(reservation.getReservationTime());
            dto.setEntryQrCode(reservation.getEntryQrCode());
            dto.setExitQrCode(reservation.getExitQrCode());
            dto.setQrExpiryTime(reservation.getQrExpiryTime());
            dto.setStatus(reservation.getStatus());
            reservationDTOList.add(dto);
        }
        return reservationDTOList;
    }

    // 获取所有预约信息
    public List<ReservationDTO> getAllReservations() {
        // 获取所有预约记录
        List<Reservation> reservations = reservationMapper.selectByExample(null);

        // 将每个 Reservation 转换为 ReservationDTO 并返回列表
        return reservations.stream()
                .map(this::convertToDTO) // 使用转换方法转换成 DTO
                .collect(Collectors.toList());
    }

    public boolean isCancellable(int reservationId) {
        // 查找预约记录，获取对应的 gym_timeslot_id
        ReservationExample example = new ReservationExample();
        example.createCriteria().andReservationidEqualTo(reservationId);
        List<Reservation> reservations = reservationMapper.selectByExample(example);

        if (reservations.isEmpty()) {
            return false;  // 预约不存在
        }

        Reservation reservation = reservations.get(0);
        int gymTimeslotId = reservation.getTimeslotid();  // 假设 reservation 中有 gym_timeslot_id 字段

        // 根据 gym_timeslot_id 查找对应的时段信息
        GymTimeslotExample gymTimeslotExample = new GymTimeslotExample();
        gymTimeslotExample.createCriteria().andIdEqualTo(gymTimeslotId);
        List<GymTimeslot> gymTimeslots = gymTimeslotMapper.selectByExample(gymTimeslotExample);

        if (gymTimeslots.isEmpty()) {
            return false;  // 未找到对应的时段
        }

        GymTimeslot gymTimeslot = gymTimeslots.get(0);
        LocalDateTime startTime = gymTimeslot.getDate().atTime(gymTimeslot.getStartTime());  // 拼接 date 和 start_time

        // 获取当前时间
        long currentTime = System.currentTimeMillis();

        // 获取时段的开始时间
        long startTimeMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 判断是否在时段开始前30分钟以内
        return (startTimeMillis - currentTime) > 30 * 60 * 1000;  // 还可以取消
    }

    // 你可以根据需要定义一个转换方法，将 Reservation 转换为 ReservationDTO
    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationId(reservation.getReservationid());
        dto.setGymId(reservation.getGymid());
        dto.setUserId(reservation.getUserid());
        dto.setTimeslotId(reservation.getTimeslotid());
        dto.setReservationTime(reservation.getReservationTime());
        // 二维码信息
        dto.setEntryQrCode(reservation.getEntryQrCode());
        dto.setExitQrCode(reservation.getExitQrCode());
        dto.setQrExpiryTime(reservation.getQrExpiryTime());

        return dto;
    }




//    public boolean reserveGym(ReservationRequest request) {
//        // 查询时段信息
//        GymTimeslot timeslot = gymTimeslotMapper.selectByPrimaryKey(request.getTimeslotId());
//        if (timeslot == null || timeslot.getCurrentReservations() >= timeslot.getMaxCapacity()) {
//            return false; // 已满
//        }
//
//        // 插入预约记录
//        Reservation reservation = new Reservation();
//        reservation.setUserid(request.getUserId());
//        reservation.setGymid(request.getGymId());
//        reservation.setTimeslotid(request.getTimeslotId());
//        reservation.setStatus("booked"); // 设置状态为 booked
//        reservation.setReservationTime(LocalDateTime.now());
//
//        // 先插入预约记录，获取自动生成的 reservationId
//        reservationMapper.insert(reservation);  // 这里假设 reservationMapper 已经配置为返回主键
//
//        // 获取自动生成的 reservationId
//        int reservationId = reservation.getReservationid();
//
//        // 生成二维码（将 reservationId 传入生成二维码方法）
//        reservation.setEntryQrCode(generateQrCode(request.getUserId(), request.getGymId(), "entry", reservationId));
//        reservation.setExitQrCode(generateQrCode(request.getUserId(), request.getGymId(), "exit", reservationId));
//
//        // 更新时段表的 current_reservations +1
//        timeslot.setCurrentReservations(timeslot.getCurrentReservations() + 1);
//        gymTimeslotMapper.updateByPrimaryKeySelective(timeslot);
//
//        return true;
//    }

//    @Autowired
//    private QRCodeService qrCodeService;

//    public String generateQrCode(String userId, int gymId, String type) {
//        String uniqueCode = UUID.randomUUID().toString(); // 生成唯一标识
//        return gymId + "_" + userId + "_" + type + "_" + uniqueCode;
//    }


//    public boolean reserveGym(ReservationRequest request) {
//        // 查询时段信息
//        GymTimeslot timeslot = gymTimeslotMapper.selectByPrimaryKey(request.getTimeslotId());
//        if (timeslot == null || timeslot.getCurrentReservations() >= timeslot.getMaxCapacity()) {
//            return false; // 已满
//        }
//
//
//        // 插入预约记录
//        Reservation reservation = new Reservation();
//        reservation.setUserid(request.getUserId());
//        reservation.setGymid(request.getGymId());
//        reservation.setTimeslotid(request.getTimeslotId());
//        reservation.setStatus("booked"); // 设置状态为 booked
//        //reservation.setQrCode(UUID.randomUUID().toString()); // 生成二维码
//        reservation.setEntryQrCode(generateQrCode(request.getUserId(), request.getGymId(), "entry"));
//        reservation.setExitQrCode(generateQrCode(request.getUserId(), request.getGymId(), "exit"));
//        reservation.setReservationTime(LocalDateTime.now());
//        reservationMapper.insert(reservation);
//
////        // 生成进出二维码(需要先插入以获取ID)
////        String entryQR = qrCodeService.generateEntryQRCode(
////                reservation.getReservationid().toString(),
////                reservation.getUserid(),
////                reservation.getGymid()
////        );
////
////        String exitQR = qrCodeService.generateExitQRCode(
////                reservation.getReservationid().toString(),
////                reservation.getUserid(),
////                reservation.getGymid()
////        );
////
////        // 设置二维码信息和24小时有效期
////        reservation.setEntryQrCode(entryQR);
////        reservation.setExitQrCode(exitQR);
////        reservation.setQrExpiryTime(LocalDateTime.now().plusHours(36));
//
//        // 更新时段表的 current_reservations +1
//        timeslot.setCurrentReservations(timeslot.getCurrentReservations() + 1);
//        gymTimeslotMapper.updateByPrimaryKeySelective(timeslot);
//
//        return true;
//    }

//    public boolean reserveGym(ReservationRequest request) {
//        // 查询时段信息
//        GymTimeslot timeslot = gymTimeslotMapper.selectByPrimaryKey(request.getTimeslotId());
//        if (timeslot == null || timeslot.getCurrentReservations() >= timeslot.getMaxCapacity()) {
//            return false; // 已满
//        }
//
//        // 插入预约记录
//        Reservation reservation = new Reservation();
//        reservation.setUserid(request.getUserId());
//        reservation.setGymid(request.getGymId());
//        reservation.setTimeslotid(request.getTimeslotId());
//        reservation.setStatus("active");
//        reservation.setQrCode(UUID.randomUUID().toString()); // 生成二维码
//        reservation.setReservationTime(LocalDateTime.now()); // 使用 LocalDateTime
//        reservationMapper.insert(reservation);
//        return true;
//    }

//    public boolean cancelReservation(int reservationId) {
//        ReservationExample example = new ReservationExample();
//        example.createCriteria().andReservationidEqualTo(reservationId);
//        List<Reservation> reservations = reservationMapper.selectByExample(example);
//        if (reservations.isEmpty() || !reservations.get(0).isCancellable()) {
//            return false;
//        }
//
//        // 删除预约记录
//        reservationMapper.deleteByExample(example);
//        return true;
//    }

//    public boolean cancelReservation(int reservationId) {
//        ReservationExample example = new ReservationExample();
//        example.createCriteria().andReservationidEqualTo(reservationId);
//        int deletedRows = reservationMapper.deleteByExample(example);
//        return deletedRows > 0;
//    }

}
