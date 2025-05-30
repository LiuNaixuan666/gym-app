package com.liu.gymmanagement.model;

import java.time.LocalDateTime;
import java.util.Date;

public class CapacityLog {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column capacity_logs.LogID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private Integer logid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column capacity_logs.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private Integer gymid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column capacity_logs.current_count
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private Integer currentCount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column capacity_logs.log_time
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private LocalDateTime logTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column capacity_logs.LogID
     *
     * @return the value of capacity_logs.LogID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public Integer getLogid() {
        return logid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column capacity_logs.LogID
     *
     * @param logid the value for capacity_logs.LogID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setLogid(Integer logid) {
        this.logid = logid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column capacity_logs.GymID
     *
     * @return the value of capacity_logs.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public Integer getGymid() {
        return gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column capacity_logs.GymID
     *
     * @param gymid the value for capacity_logs.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setGymid(Integer gymid) {
        this.gymid = gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column capacity_logs.current_count
     *
     * @return the value of capacity_logs.current_count
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public Integer getCurrentCount() {
        return currentCount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column capacity_logs.current_count
     *
     * @param currentCount the value for capacity_logs.current_count
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column capacity_logs.log_time
     *
     * @return the value of capacity_logs.log_time
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public LocalDateTime getLogTime() {
        return logTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column capacity_logs.log_time
     *
     * @param logTime the value for capacity_logs.log_time
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }
}