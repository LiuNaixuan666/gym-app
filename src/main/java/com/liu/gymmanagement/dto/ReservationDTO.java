package com.liu.gymmanagement.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class ReservationDTO {
    private int reservationId;
    private int gymId;
    private int timeslotId;
    private String userId;
    private LocalDateTime reservationTime;
    private String entryQrCode;
    private String exitQrCode;
    private LocalDateTime qrExpiryTime;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getGymId() {
        return gymId;
    }

    public void setGymId(int gymId) {
        this.gymId = gymId;
    }

    public int getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(int timeslotId) {
        this.timeslotId = timeslotId;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntryQrCode() {
        return entryQrCode;
    }

    public void setEntryQrCode(String entryQrCode) {
        this.entryQrCode = entryQrCode;
    }

    public String getExitQrCode() {
        return exitQrCode;
    }

    public void setExitQrCode(String exitQrCode) {
        this.exitQrCode = exitQrCode;
    }

    public LocalDateTime getQrExpiryTime() {
        return qrExpiryTime;
    }

    public void setQrExpiryTime(LocalDateTime qrExpiryTime) {
        this.qrExpiryTime = qrExpiryTime;
    }


}