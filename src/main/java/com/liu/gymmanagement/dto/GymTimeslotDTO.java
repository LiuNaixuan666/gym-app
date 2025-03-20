package com.liu.gymmanagement.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class GymTimeslotDTO {

    private Integer timeslotId; // 时段ID
    private LocalTime startTime; // 时段开始时间
    private LocalTime endTime; // 时段结束时间
    private Integer maxCapacity; // 最大预约人数
    private Integer reservedCount; // 已预约人数 (可以通过查询预约情况来计算)

    // Getters and Setters
    public Integer getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(Integer timeslotId) {
        this.timeslotId = timeslotId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }
}

