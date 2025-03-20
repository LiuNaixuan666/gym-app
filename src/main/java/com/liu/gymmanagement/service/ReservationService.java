package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.ReservationDTO;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.mapper.ReservationMapper;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.Reservation;
import com.liu.gymmanagement.dto.ReservationRequest;
import com.liu.gymmanagement.model.ReservationExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        reservation.setStatus("active");
        reservation.setQrCode(UUID.randomUUID().toString()); // 生成二维码
        reservation.setReservationTime(LocalDateTime.now()); // 使用 LocalDateTime
        reservationMapper.insert(reservation);
        return true;
    }

    public boolean cancelReservation(int reservationId) {
        ReservationExample example = new ReservationExample();
        example.createCriteria().andReservationidEqualTo(reservationId);
        List<Reservation> reservations = reservationMapper.selectByExample(example);
        if (reservations.isEmpty() || !reservations.get(0).isCancellable()) {
            return false;
        }

        // 删除预约记录
        reservationMapper.deleteByExample(example);
        return true;
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

    // 你可以根据需要定义一个转换方法，将 Reservation 转换为 ReservationDTO
    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationId(reservation.getReservationid());
        dto.setGymId(reservation.getGymid());
        dto.setUserId(reservation.getUserid());
        dto.setTimeslotId(reservation.getTimeslotid());
        dto.setReservationTime(reservation.getReservationTime());
        return dto;
    }
}
