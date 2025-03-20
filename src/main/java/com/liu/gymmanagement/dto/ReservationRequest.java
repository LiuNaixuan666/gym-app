package com.liu.gymmanagement.dto;

public class ReservationRequest {
    private String userId;  // 学生ID
    private int gymId;      // 健身房ID
    private int timeslotId; // 预约时段ID

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
